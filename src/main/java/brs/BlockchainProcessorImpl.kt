package brs

import brs.Constants.FEE_QUANT
import brs.Constants.ONE_BURST
import brs.at.AT
import brs.at.AtBlock
import brs.at.AtException
import brs.crypto.Crypto
import brs.fluxcapacitor.FluxValues
import brs.peer.Peer
import brs.props.Props
import brs.taskScheduler.RepeatingTask
import brs.taskScheduler.TaskType
import brs.transactionduplicates.TransactionDuplicatesCheckerImpl
import brs.unconfirmedtransactions.UnconfirmedTransactionStore
import brs.util.*
import brs.util.convert.parseUnsignedLong
import brs.util.convert.safeSubtract
import brs.util.convert.toUnsignedString
import brs.util.delegates.Atomic
import brs.util.logging.*
import brs.util.sync.Mutex
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max

class BlockchainProcessorImpl(private val dp: DependencyProvider) : BlockchainProcessor {
    override val oclVerify = dp.propertyService.get(Props.GPU_ACCELERATION)
    private val oclUnverifiedQueue = dp.propertyService.get(Props.GPU_UNVERIFIED_QUEUE)

    private val gpuUsage = Semaphore(2)

    private val trimDerivedTables = dp.propertyService.get(Props.DB_TRIM_DERIVED_TABLES)
    private val lastTrimHeight = AtomicInteger()
    
    private val processMutex = Mutex()

    private val blockListeners = Listeners<Block, BlockchainProcessor.Event>()
    override var lastBlockchainFeeder by Atomic<Peer?>()
    override var lastBlockchainFeederHeight by Atomic<Int?>()

    private val autoPopOffEnabled = dp.propertyService.get(Props.AUTO_POP_OFF_ENABLED)
    private var autoPopOffLastStuckHeight = 0
    private var autoPopOffNumberOfBlocks = 0

    private var peerHasMore = false

    private val getCumulativeDifficultyRequest by lazy {
        val request = JsonObject()
        request.addProperty("requestType", "getCumulativeDifficulty")
        JSON.prepareRequest(request)
    }

    private val getMoreBlocksTask: RepeatingTask = {
        run {
            if (dp.propertyService.get(Props.DEV_OFFLINE)) return@run false
            try {
                // unlocking cache for writing.
                // This must be done before we query where to add blocks.
                // We sync the cache in event of pop off
                dp.downloadCache.mutex.withLock {
                    dp.downloadCache.unlockCache()
                }


                if (dp.downloadCache.isFull) {
                    return@run false
                }
                peerHasMore = true
                val peer = dp.peers.getAnyPeer(Peer.State.CONNECTED)
                if (peer == null) {
                    logger.safeDebug { "No peer connected." }
                    return@run false
                }
                val response = peer.send(getCumulativeDifficultyRequest) ?: return@run true
                if (response["blockchainHeight"] != null) {
                    lastBlockchainFeeder = peer
                    lastBlockchainFeederHeight = response["blockchainHeight"].mustGetAsInt("blockchainHeight")
                } else {
                    logger.safeDebug { "Peer has no chainheight" }
                    return@run true
                }

                /* Cache now contains Cumulative Difficulty */
                val curCumulativeDifficulty = dp.downloadCache.cumulativeDifficulty
                val peerCumulativeDifficulty = response.get("cumulativeDifficulty").mustGetAsString("cumulativeDifficulty")
                if (peerCumulativeDifficulty.isEmpty()) {
                    logger.safeDebug { "Peer CumulativeDifficulty is null" }
                    return@run true
                }
                val betterCumulativeDifficulty = BigInteger(peerCumulativeDifficulty)
                if (betterCumulativeDifficulty <= curCumulativeDifficulty) {
                    return@run true
                }

                var commonBlockId = Genesis.GENESIS_BLOCK_ID
                val cacheLastBlockId = dp.downloadCache.getLastBlockId()

                // Now we will find the highest common block between us and the peer
                if (cacheLastBlockId != Genesis.GENESIS_BLOCK_ID) {
                    commonBlockId = getCommonMilestoneBlockId(peer)
                    if (commonBlockId == 0L || !peerHasMore) {
                        logger.safeDebug { "We could not get a common milestone block from peer." }
                        return@run true
                    }
                }

                /**
                 * if we did not get the last block in chain as common block we will be downloading a
                 * fork. however if it is to far off we cannot process it anyway. canBeFork will check
                 * where in chain this common block is fitting and return true if it is worth to
                 */

                var saveInCache = true
                if (commonBlockId != cacheLastBlockId) {
                    if (dp.downloadCache.canBeFork(commonBlockId)) {
                        // the fork is not that old. Lets see if we can get more precise.
                        commonBlockId = getCommonBlockId(peer, commonBlockId)
                        if (commonBlockId == 0L || !peerHasMore) {
                            logger.safeDebug { "Trying to get a more precise common block resulted in an error." }
                            return@run true
                        }
                        saveInCache = false
                        dp.downloadCache.resetForkBlocks()
                    } else {
                        logger.safeWarn { "Our peer want to feed us a fork that is more than ${dp.propertyService.get(Props.DB_MAX_ROLLBACK)} blocks old." }
                        return@run true
                    }
                }

                val nextBlocks = getNextBlocks(peer, commonBlockId)
                if (nextBlocks == null || nextBlocks.size() == 0) {
                    logger.safeDebug { "Peer did not feed us any blocks" }
                    return@run true
                }

                // download blocks from peer
                var lastBlock = dp.downloadCache.getBlock(commonBlockId)
                if (lastBlock == null) {
                    logger.safeInfo { "Error: lastBlock is null" }
                    return@run true
                }

                // loop blocks and make sure they fit in chain
                var block: Block
                var blockData: JsonObject

                for (o in nextBlocks) {
                    val height = lastBlock!!.height + 1
                    blockData = o.mustGetAsJsonObject("nextBlock")
                    try {
                        block = Block.parseBlock(dp, blockData, height)
                        // Make sure it maps back to chain
                        if (lastBlock.id != block.previousBlockId) {
                            logger.safeDebug { "Discarding downloaded data. Last downloaded blocks is rubbish" }
                            logger.safeDebug { "DB blockID: ${lastBlock?.id} DB blockheight: ${lastBlock?.height} Downloaded previd: ${block.previousBlockId}" }
                            return@run true
                        }
                        // set height and cumulative difficulty to block
                        block.height = height
                        block.peer = peer
                        block.byteLength = blockData.toJsonString().length
                        dp.blockService.calculateBaseTarget(block, lastBlock)
                        if (saveInCache) {
                            if (dp.downloadCache.getLastBlockId() == block.previousBlockId) { //still maps back? we might have got announced/forged blocks
                                if (!dp.downloadCache.addBlock(block)) {
                                    //we stop the loop since cahce has been locked
                                    return@run true
                                }
                                logger.safeDebug { "Added from download: Id: ${block.id} Height: ${block.height}" }
                            }
                        } else {
                            dp.downloadCache.addForkBlock(block)
                        }
                        lastBlock = block
                    } catch (e: BlockchainProcessor.BlockOutOfOrderException) {
                        logger.safeInfo(e) { "$e - autoflushing cache to get rid of it" }
                        dp.downloadCache.resetCache()
                        return@run false
                    } catch (e: RuntimeException) {
                        logger.safeInfo(e) { "Failed to parse block: $e" }
                        logger.safeInfo { "Failed to parse block trace: ${e.stackTrace.joinToString()}" }
                        peer.blacklist(e, "pulled invalid data using getCumulativeDifficulty")
                        return@run false
                    } catch (e: BurstException.ValidationException) {
                        logger.safeInfo(e) { "Failed to parse block: $e" }
                        logger.safeInfo { "Failed to parse block trace: ${e.stackTrace.joinToString()}" }
                        peer.blacklist(e, "pulled invalid data using getCumulativeDifficulty")
                        return@run false
                    } catch (e: Exception) {
                        logger.safeWarn(e) { "Unhandled exception $e" }
                        logger.safeWarn { "Unhandled exception trace: ${e.stackTrace.joinToString()}" }
                        return@run false
                    }
                }

                logger.safeTrace { "Unverified blocks: ${dp.downloadCache.unverifiedSize}" }
                logger.safeTrace { "Blocks in cache: ${dp.downloadCache.size()}" }
                logger.safeTrace { "Bytes in cache: ${dp.downloadCache.blockCacheSize}" }
                if (!saveInCache) {
                    /*
                     * Since we cannot rely on peers reported cumulative difficulty we do
                     * a final check to see that the CumulativeDifficulty actually is bigger
                     * before we do a popOff and switch chain.
                     */
                    if (lastBlock!!.cumulativeDifficulty < curCumulativeDifficulty) {
                        peer.blacklist("peer claimed to have bigger cumulative difficulty but in reality it did not.")
                        dp.downloadCache.resetForkBlocks()
                        return@run true
                    }
                    processFork(peer, dp.downloadCache.forkList, commonBlockId)
                }

            } catch (e: BurstException.StopException) {
                logger.safeInfo { "Blockchain download stopped: ${e.message}" }
            } catch (e: Exception) {
                logger.safeInfo(e) { "Error in blockchain download thread" }
            }
            return@run true
        }
    }

    private val blockImporterTask: RepeatingTask = {
        if (dp.downloadCache.size() == 0) {
            false
        } else {
            try {
                val lastBlock = dp.blockchain.lastBlock
                val lastId = lastBlock.id
                val currentBlock = dp.downloadCache.getNextBlock(lastId) /* this should fetch first block in cache */
                if (currentBlock == null || currentBlock.height != lastBlock.height + 1) {
                    logger.safeDebug { "cache is reset due to orphaned block(s). CacheSize: ${dp.downloadCache.size()}" }
                    dp.downloadCache.resetCache() //resetting cache because we have blocks that cannot be processed.
                    false
                } else {
                    try {
                        if (!currentBlock.isVerified) {
                            dp.downloadCache.removeUnverified(currentBlock.id)
                            dp.blockService.preVerify(currentBlock)
                            logger.safeDebug { "block was not preverified" }
                        }
                        pushBlock(currentBlock) // This removes the block from cache.
                        true
                    } catch (e: BlockchainProcessor.BlockNotAcceptedException) {
                        logger.safeError(e) { "Block not accepted" }
                        blacklistClean(currentBlock, e, "found invalid pull/push data during importing the block")
                        autoPopOff(currentBlock.height)
                        false
                    }
                }
            } catch (e: Exception) {
                logger.safeError(e) { "Uncaught exception in blockImporterTask" }
                false
            }
        }
    }

    private val pocVerificationTask: RepeatingTask = {
        var verifyWithOcl: Boolean
        val queueThreshold = if (oclVerify) oclUnverifiedQueue else 0
        val unVerified = dp.downloadCache.unverifiedSize
        if (unVerified > queueThreshold) { // Is there anything to verify?
            if (unVerified >= oclUnverifiedQueue && oclVerify) { //should we use Ocl?
                verifyWithOcl = true
                if (!gpuUsage.tryAcquire()) { //is Ocl ready ?
                    logger.safeDebug { "already max locked" }
                    verifyWithOcl = false
                }
            } else {
                verifyWithOcl = false
            }
            if (verifyWithOcl && dp.oclPoC != null) {
                val pocVersion =
                    dp.downloadCache.getPoCVersion(dp.downloadCache.getUnverifiedBlockIdFromPos(0))
                var pos = 0
                val blocks = LinkedList<Block>()
                while (dp.downloadCache.unverifiedSize - 1 > pos && blocks.size < dp.oclPoC!!.maxItems) {
                    val blockId = dp.downloadCache.getUnverifiedBlockIdFromPos(pos)
                    if (dp.downloadCache.getPoCVersion(blockId) != pocVersion) {
                        break
                    }
                    blocks.add(dp.downloadCache.getBlock(blockId)!!)
                    pos += 1
                }
                try {
                    dp.oclPoC!!.validatePoC(blocks, pocVersion, dp.blockService)
                    dp.downloadCache.removeUnverifiedBatch(blocks)
                } catch (e: OCLPoC.PreValidateFailException) {
                    logger.safeInfo(e) { e.toString() }
                    blacklistClean(e.block, e, "found invalid pull/push data during processing the pocVerification")
                } catch (e: OCLPoC.OCLCheckerException) {
                    logger.safeInfo(e) { "Open CL error. slow verify will occur for the next $oclUnverifiedQueue Blocks" }
                } catch (e: Exception) {
                    logger.safeInfo(e) { "Unspecified Open CL error: " }
                } finally {
                    gpuUsage.release()
                }
            } else { //verify using java
                try {
                    val unverifiedBlock = dp.downloadCache.firstUnverifiedBlock
                    if (unverifiedBlock != null) {
                        dp.blockService.preVerify(unverifiedBlock)
                    }
                } catch (e: BlockchainProcessor.BlockNotAcceptedException) {
                    logger.safeError(e) { "Block failed to preverify: " }
                }
            }
            true
        } else {
            false
        }
    }

    override val minRollbackHeight: Int
        get() {
            val trimHeight = if (lastTrimHeight.get() > 0)
                lastTrimHeight.get()
            else
                max(dp.blockchain.height - dp.propertyService.get(Props.DB_MAX_ROLLBACK), 0)
            return if (trimDerivedTables) trimHeight else 0
        }

    private val blockVersion: Int
        get() = 3

    init {
        dp.taskScheduler.scheduleTask(TaskType.IO, this.getMoreBlocksTask) // TODO somehow parallelize this task
        dp.taskScheduler.scheduleTask(TaskType.COMPUTATION, this.blockImporterTask)

        // TODO have separate tasks depending on whether we want to use GPU acceleration
        if (dp.propertyService.get(Props.GPU_ACCELERATION)) {
            logger.safeDebug { "Starting pre-verifier thread in Open CL mode." }
            dp.taskScheduler.scheduleTask(TaskType.IO, this.pocVerificationTask)
        } else {
            logger.safeDebug { "Starting pre-verifier thread in CPU mode." }
            dp.taskScheduler.scheduleTask(TaskType.COMPUTATION, Runtime.getRuntime().availableProcessors(), this.pocVerificationTask) // TODO property for number of instances
        }

        blockListeners.addListener(BlockchainProcessor.Event.BLOCK_SCANNED) { block ->
            if (block.height % 5000 == 0) {
                logger.safeInfo { "processed block ${block.height}" }
            }
        }

        blockListeners.addListener(BlockchainProcessor.Event.BLOCK_PUSHED) { block ->
            if (block.height % 5000 == 0) {
                logger.safeInfo { "processed block ${block.height}" }
            }
        }

        blockListeners.addListener(BlockchainProcessor.Event.BLOCK_PUSHED) { dp.transactionProcessor.revalidateUnconfirmedTransactions() }

        if (trimDerivedTables) {
            blockListeners.addListener(BlockchainProcessor.Event.AFTER_BLOCK_APPLY) { block ->
                if (block.height % 1440 == 0) {
                    lastTrimHeight.set(max(block.height - dp.propertyService.get(Props.DB_MAX_ROLLBACK), 0))
                    if (lastTrimHeight.get() > 0) {
                        dp.derivedTableManager.derivedTables.forEach { table -> table.trim(lastTrimHeight.get()) }
                    }
                }
            }
        }

        addGenesisBlock()
    }

    private fun getCommonMilestoneBlockId(peer: Peer?): Long {
        var lastMilestoneBlockId: String? = null
        while (true) {
            val milestoneBlockIdsRequest = JsonObject()
            milestoneBlockIdsRequest.addProperty("requestType", "getMilestoneBlockIds")
            if (lastMilestoneBlockId == null) {
                milestoneBlockIdsRequest.addProperty("lastBlockId",
                    dp.downloadCache.getLastBlockId().toUnsignedString())
            } else {
                milestoneBlockIdsRequest.addProperty("lastMilestoneBlockId", lastMilestoneBlockId)
            }

            val response = peer!!.send(JSON.prepareRequest(milestoneBlockIdsRequest))
            if (response == null) {
                logger.safeDebug { "Got null response in getCommonMilestoneBlockId" }
                return 0
            }
            val milestoneBlockIds = response.get("milestoneBlockIds").safeGetAsJsonArray()
            if (milestoneBlockIds == null || milestoneBlockIds.isEmpty()) {
                logger.safeDebug { "MilestoneArray is empty" }
                return 0
            }
            if (milestoneBlockIds.isEmpty()) {
                return Genesis.GENESIS_BLOCK_ID
            }
            // prevent overloading with blockIds
            if (milestoneBlockIds.size() > 20) {
                peer.blacklist("obsolete or rogue peer sends too many milestoneBlockIds")
                return 0
            }
            if (response.get("last").safeGetAsBoolean() == true) {
                peerHasMore = false
            }

            for (milestoneBlockId in milestoneBlockIds) {
                val milestoneBlockIdString = milestoneBlockId.mustGetAsString("milestoneBlockId")
                val blockId = milestoneBlockIdString.parseUnsignedLong()

                if (dp.downloadCache.hasBlock(blockId)) {
                    if (lastMilestoneBlockId == null && milestoneBlockIds.size() > 1) {
                        peerHasMore = false
                        logger.safeDebug { "Peer dont have more (cache)" }
                    }
                    return blockId
                }
                lastMilestoneBlockId = milestoneBlockIdString
            }
        }
    }

    private fun getCommonBlockId(peer: Peer?, commonBlockId: Long): Long {
        var commonBlockId = commonBlockId

        while (true) { // TODO not while true
            val request = JsonObject()
            request.addProperty("requestType", "getNextBlockIds")
            request.addProperty("blockId", commonBlockId.toUnsignedString())
            val response = peer!!.send(JSON.prepareRequest(request)) ?: return 0
            val nextBlockIds = response.get("nextBlockIds").mustGetAsJsonArray("enxtBlockIds")
            if (nextBlockIds.isEmpty()) return 0
            // prevent overloading with blockIds
            if (nextBlockIds.size() > 1440) { // TODO just truncate
                peer.blacklist("obsolete or rogue peer sends too many nextBlocks")
                return 0
            }

            for (nextBlockId in nextBlockIds) {
                val blockId = nextBlockId.mustGetAsString("nextBlockId").parseUnsignedLong()
                if (!dp.downloadCache.hasBlock(blockId)) {
                    return commonBlockId
                }
                commonBlockId = blockId
            }
        }
    }

    private fun getNextBlocks(peer: Peer?, curBlockId: Long): JsonArray? {
        val request = JsonObject()
        request.addProperty("requestType", "getNextBlocks")
        request.addProperty("blockId", curBlockId.toUnsignedString())
        logger.safeDebug { "Getting next Blocks after ${curBlockId.toUnsignedString()} from ${peer!!.peerAddress}" }
        val response = peer!!.send(JSON.prepareRequest(request)) ?: return null

        val nextBlocks = response.get("nextBlocks").safeGetAsJsonArray()
        if (nextBlocks == null || nextBlocks.isEmpty()) return null
        // prevent overloading with blocks
        if (nextBlocks.size() > 1440) {
            peer.blacklist("obsolete or rogue peer sends too many nextBlocks")
            return null
        }
        logger.safeDebug { "Got ${nextBlocks.size()} blocks after ${curBlockId.toUnsignedString()} from ${peer.peerAddress}" }
        return nextBlocks

    }

    private fun processFork(peer: Peer, forkBlocks: List<Block>, forkBlockId: Long) {
        logger.safeWarn { "A fork is detected. Waiting for cache to be processed." }
        dp.downloadCache.lockCache() //dont let anything add to cache!
        while (dp.downloadCache.size() != 0) Thread.sleep(1000) // TODO don't do this...
        dp.downloadCache.mutex.withLock {
            processMutex.withLock {
                logger.safeWarn { "Cache is now processed. Starting to process fork." }
                val forkBlock = dp.blockchain.getBlock(forkBlockId)

                // we read the current cumulative difficulty
                val curCumulativeDifficulty = dp.blockchain.lastBlock.cumulativeDifficulty

                // We remove blocks from chain back to where we start our fork
                // and save it in a list if we need to restore
                val myPoppedOffBlocks = popOffTo(forkBlock!!)

                // now we check that our chain is popped off.
                // If all seems ok is we try to push fork.
                var pushedForkBlocks = 0
                if (dp.blockchain.lastBlock.id == forkBlockId) {
                    for (block in forkBlocks) {
                        if (dp.blockchain.lastBlock.id == block.previousBlockId) {
                            try {
                                dp.blockService.preVerify(block)
                                pushBlock(block)
                                pushedForkBlocks += 1
                            } catch (e: BlockchainProcessor.BlockNotAcceptedException) {
                                peer.blacklist(e, "during processing a fork")
                                break
                            }

                        }
                    }
                }

                /*
                 * we check if we succeeded to push any block. if we did we check against cumulative
                 * difficulty If it is lower we blacklist peer and set chain to be processed later.
                 */
                if (pushedForkBlocks > 0 && dp.blockchain.lastBlock.cumulativeDifficulty < curCumulativeDifficulty) {
                    logger.safeWarn { "Fork was bad and pop off was caused by peer ${peer.peerAddress}, blacklisting" }
                    peer.blacklist("got a bad fork")
                    val peerPoppedOffBlocks = popOffTo(forkBlock)
                    pushedForkBlocks = 0
                    peerPoppedOffBlocks.forEach { block -> dp.transactionProcessor.processLater(block.transactions) }
                }

                // if we did not push any blocks we try to restore chain.
                if (pushedForkBlocks == 0) {
                    for (i in myPoppedOffBlocks.indices.reversed()) {
                        val block = myPoppedOffBlocks.removeAt(i)
                        try {
                            dp.blockService.preVerify(block)
                            pushBlock(block)
                        } catch (e: BlockchainProcessor.BlockNotAcceptedException) {
                            logger.safeWarn(e) { "Popped off block no longer acceptable: " + block.toJsonObject().toJsonString() }
                            break
                        }
                    }
                } else {
                    myPoppedOffBlocks.forEach { block -> dp.transactionProcessor.processLater(block.transactions) }
                    logger.safeWarn { "Successfully switched to better chain." }
                }
                logger.safeWarn { "Forkprocessing complete." }
                dp.downloadCache.resetForkBlocks()
                dp.downloadCache.resetCache() // Reset and set cached vars to chaindata.
            }
        }
    }

    private fun blacklistClean(block: Block?, e: Exception, description: String) {
        logger.safeDebug { "Blacklisting peer and cleaning cache queue" }
        if (block == null) {
            return
        }
        val peer = block.peer
        peer?.blacklist(e, description)
        dp.downloadCache.resetCache()
        logger.safeDebug { "Blacklisted peer and cleaned queue" }
    }

    private fun autoPopOff(height: Int) {
        if (!autoPopOffEnabled) {
            logger.safeWarn { "Not automatically popping off as it is disabled via properties. If your node becomes stuck you will need to manually pop off." }
            return
        }
        processMutex.withLock {
            logger.safeWarn { "Auto popping off as failed to push block" }
            if (height != autoPopOffLastStuckHeight) {
                autoPopOffLastStuckHeight = height
                autoPopOffNumberOfBlocks = 0
            }
            if (autoPopOffNumberOfBlocks == 0) {
                logger.safeWarn { "Not popping anything off as this was the first failure at this height" }
            } else {
                logger.safeWarn { "Popping off $autoPopOffNumberOfBlocks blocks due to previous failures to push this block" }
                popOffTo(dp.blockchain.height - autoPopOffNumberOfBlocks)
            }
            autoPopOffNumberOfBlocks++
        }
    }

    override fun addListener(eventType: BlockchainProcessor.Event, listener: (Block) -> Unit) {
        return blockListeners.addListener(eventType, listener)
    }

    override fun processPeerBlock(request: JsonObject, peer: Peer) {
        val newBlock = Block.parseBlock(dp, request, dp.blockchain.height)
        //* This process takes care of the blocks that is announced by peers We do not want to be fed forks.
        val chainblock = dp.downloadCache.lastBlock
        if (chainblock!!.id == newBlock.previousBlockId) {
            newBlock.height = chainblock.height + 1
            newBlock.byteLength = newBlock.toString().length
            dp.blockService.calculateBaseTarget(newBlock, chainblock)
            dp.downloadCache.addBlock(newBlock)
            logger.safeDebug { "Peer ${peer.peerAddress} added block from Announce: Id: ${newBlock.id} Height: ${newBlock.height}" }
        } else {
            logger.safeDebug { "Peer ${peer.peerAddress} sent us block: ${newBlock.previousBlockId} which is not the follow-up block for ${chainblock.id}" }
        }
    }

    override fun popOffTo(height: Int): List<Block> {
        return popOffTo(dp.blockchain.getBlockAtHeight(height)!!)
    }

    override fun fullReset() {
        dp.blockDb.deleteAll(false)
        dp.dbCacheManager.flushCache()
        dp.downloadCache.resetCache()
        addGenesisBlock()
    }

    private fun addBlock(block: Block) {
        dp.blockchainStore.addBlock(block)
        dp.blockchain.lastBlock = block
    }

    private fun addGenesisBlock() {
        if (dp.blockDb.hasBlock(Genesis.GENESIS_BLOCK_ID)) {
            logger.safeInfo { "Genesis block already in database" }
            val lastBlock = dp.blockDb.findLastBlock()!!
            dp.blockchain.lastBlock = lastBlock
            logger.safeInfo { "Last block height: ${lastBlock.height}" }
            return
        }
        logger.safeInfo { "Genesis block not in database, starting from scratch" }
        try {
            val genesisBlock = Block(dp, -1, 0, 0, 0, 0, 0,
                    Crypto.sha256().digest(), Genesis.creatorPublicKey, ByteArray(32),
                    Genesis.genesisBlockSignature, null, emptyList(), 0, byteArrayOf(), -1)
            dp.blockService.setPrevious(genesisBlock, null)
            addBlock(genesisBlock)
        } catch (e: BurstException.ValidationException) {
            logger.safeInfo { e.message ?: e.toString() }
            throw RuntimeException(e.toString(), e)
        }
    }

    private fun pushBlock(block: Block) {
        processMutex.withLock {
            dp.db.beginTransaction()
            val curTime = dp.timeService.epochTime
            var lastBlock: Block? = null
            try {
                lastBlock = dp.blockchain.lastBlock


                if (lastBlock.id != block.previousBlockId) {
                    throw BlockchainProcessor.BlockOutOfOrderException(
                            "Previous block id doesn't match for block " + block.height
                                    + if (lastBlock.height + 1 == block.height) "" else " invalid previous height " + lastBlock.height
                    )
                }

                if (block.version != blockVersion) {
                    throw BlockchainProcessor.BlockNotAcceptedException("Invalid version " + block.version + " for block " + block.height)
                }
                if (block.version != 1 && !Arrays.equals(Crypto.sha256().digest(lastBlock.toBytes()), block.previousBlockHash)) {
                    throw BlockchainProcessor.BlockNotAcceptedException("Previous block hash doesn't match for block " + block.height)
                }
                if (block.timestamp > curTime + MAX_TIMESTAMP_DIFFERENCE || block.timestamp <= lastBlock.timestamp) {
                    throw BlockchainProcessor.BlockOutOfOrderException("Invalid timestamp: " + block.timestamp + " current time is " + curTime + ", previous block timestamp is " + lastBlock.timestamp)
                }
                if (block.id == 0L || dp.blockDb.hasBlock(block.id)) {
                    throw BlockchainProcessor.BlockNotAcceptedException("Duplicate block or invalid id for block " + block.height)
                }
                if (!dp.blockService.verifyGenerationSignature(block)) {
                    throw BlockchainProcessor.BlockNotAcceptedException("Generation signature verification failed for block " + block.height)
                }
                if (!dp.blockService.verifyBlockSignature(block)) {
                    throw BlockchainProcessor.BlockNotAcceptedException("Block signature verification failed for block " + block.height)
                }

                val transactionDuplicatesChecker = TransactionDuplicatesCheckerImpl()
                var calculatedTotalAmount: Long = 0
                var calculatedTotalFee: Long = 0
                val digest = Crypto.sha256()

                val feeArray = LongArray(block.transactions.size)
                var slotIdx = 0

                for (transaction in block.transactions) {
                    if (transaction.id == 0L)
                        throw BlockchainProcessor.TransactionNotAcceptedException("Invalid transaction id", transaction)
                    if (transaction.timestamp > curTime + MAX_TIMESTAMP_DIFFERENCE)
                        throw BlockchainProcessor.BlockOutOfOrderException("Invalid transaction timestamp: ${transaction.timestamp}, current time is $curTime")
                    if (transaction.timestamp > block.timestamp + MAX_TIMESTAMP_DIFFERENCE || transaction.expiration < block.timestamp)
                        throw BlockchainProcessor.TransactionNotAcceptedException("Invalid transaction timestamp ${transaction.timestamp} for transaction ${transaction.stringId}, current time is $curTime, block timestamp is ${block.timestamp}", transaction)
                    if (dp.transactionDb.hasTransaction(transaction.id))
                        throw BlockchainProcessor.TransactionNotAcceptedException("Transaction ${transaction.stringId} is already in the blockchain", transaction)
                    if (transaction.referencedTransactionFullHash != null && !hasAllReferencedTransactions(transaction, transaction.timestamp, 0))
                        throw BlockchainProcessor.TransactionNotAcceptedException("Missing or invalid referenced transaction ${transaction.referencedTransactionFullHash} for transaction ${transaction.stringId}", transaction)
                    if (transaction.version.toInt() != dp.transactionProcessor.getTransactionVersion(lastBlock.height))
                        throw BlockchainProcessor.TransactionNotAcceptedException("Invalid transaction version ${transaction.version} at height ${lastBlock.height}", transaction)
                    if (!dp.transactionService.verifyPublicKey(transaction))
                        throw BlockchainProcessor.TransactionNotAcceptedException("Wrong public key in transaction ${transaction.stringId} at height ${lastBlock.height}", transaction)
                    if (transactionDuplicatesChecker.hasAnyDuplicate(transaction))
                        throw BlockchainProcessor.TransactionNotAcceptedException("Transaction is a duplicate: ${transaction.stringId}", transaction)

                    if (dp.fluxCapacitor.getValue(FluxValues.AUTOMATED_TRANSACTION_BLOCK) && !dp.economicClustering.verifyFork(transaction)) {
                        logger.safeDebug { "Block ${block.stringId} height ${lastBlock.height + 1} contains transaction that was generated on a fork: ${transaction.stringId} ecBlockId ${transaction.ecBlockHeight} ecBlockHeight ${transaction.ecBlockId.toUnsignedString()}" }
                        throw BlockchainProcessor.TransactionNotAcceptedException("Transaction belongs to a different fork", transaction)
                    }

                    try {
                        dp.transactionService.validate(transaction)
                    } catch (e: BurstException.ValidationException) {
                        throw BlockchainProcessor.TransactionNotAcceptedException(e.message!!, transaction)
                    }

                    calculatedTotalAmount += transaction.amountNQT
                    calculatedTotalFee += transaction.feeNQT
                    digest.update(transaction.toBytes())
                    dp.indirectIncomingService.processTransaction(transaction)
                    feeArray[slotIdx] = transaction.feeNQT
                    slotIdx += 1
                }

                if (calculatedTotalAmount > block.totalAmountNQT || calculatedTotalFee > block.totalFeeNQT) {
                    throw BlockchainProcessor.BlockNotAcceptedException("Total amount or fee don't match transaction totals for block " + block.height)
                }

                if (dp.fluxCapacitor.getValue(FluxValues.NEXT_FORK)) {
                    Arrays.sort(feeArray)
                    for (i in feeArray.indices) {
                        if (feeArray[i] >= FEE_QUANT * (i + 1)) {
                            throw BlockchainProcessor.BlockNotAcceptedException("Transaction fee is not enough to be included in this block " + block.height)
                        }
                    }
                }

                if (!Arrays.equals(digest.digest(), block.payloadHash)) {
                    throw BlockchainProcessor.BlockNotAcceptedException("Payload hash doesn't match for block " + block.height)
                }

                val remainingAmount = block.totalAmountNQT.safeSubtract(calculatedTotalAmount)
                val remainingFee = block.totalFeeNQT.safeSubtract(calculatedTotalFee)

                dp.blockService.setPrevious(block, lastBlock)
                blockListeners.accept(BlockchainProcessor.Event.BEFORE_BLOCK_ACCEPT, block)
                dp.transactionProcessor.removeForgedTransactions(block.transactions)
                dp.transactionProcessor.requeueAllUnconfirmedTransactions()
                dp.accountService.flushAccountTable()
                addBlock(block)
                dp.downloadCache.removeBlock(block) // We make sure downloadCache do not have this block anymore.
                accept(block, remainingAmount, remainingFee)
                dp.derivedTableManager.derivedTables.forEach { it.finish() }
                dp.db.commitTransaction()
            } catch (e: BlockchainProcessor.BlockNotAcceptedException) {
                dp.db.rollbackTransaction()
                if (lastBlock != null) {
                    dp.blockchain.lastBlock = lastBlock
                }
                dp.downloadCache.resetCache()
                throw e
            } catch (e: ArithmeticException) {
                dp.db.rollbackTransaction()
                if (lastBlock != null) {
                    dp.blockchain.lastBlock = lastBlock
                }
                dp.downloadCache.resetCache()
                throw e
            } finally {
                dp.db.endTransaction()
            }
            logger.safeDebug { "Successfully pushed ${block.id.toUnsignedString()} (height ${block.height})"}
            dp.statisticsManager.blockAdded()
            blockListeners.accept(BlockchainProcessor.Event.BLOCK_PUSHED, block)
            if (block.timestamp >= dp.timeService.epochTime - MAX_TIMESTAMP_DIFFERENCE) {
                dp.peers.sendToSomePeers(block)
            }
            if (block.height >= autoPopOffLastStuckHeight) {
                autoPopOffNumberOfBlocks = 0
            }
            if (block.height % Constants.OPTIMIZE_TABLE_FREQUENCY == 0) {
                dp.derivedTableManager.derivedTables.forEach { it.optimize() } // TODO this is not all of the tables...
            }
        }
    }

    private fun accept(block: Block, remainingAmount: Long?, remainingFee: Long?) {
        dp.subscriptionService.clearRemovals()
        for (transaction in block.transactions) {
            if (!dp.transactionService.applyUnconfirmed(transaction)) {
                throw BlockchainProcessor.TransactionNotAcceptedException("Double spending transaction: " + transaction.stringId, transaction)
            }
        }

        var calculatedRemainingAmount: Long = 0
        var calculatedRemainingFee: Long = 0
        // ATs
        val atBlock: AtBlock
        AT.clearPendingFees()
        AT.clearPendingTransactions()
        try {
            atBlock = dp.atController.validateATs(block.blockATs, dp.blockchain.height)
        } catch (e: AtException) {
            throw BlockchainProcessor.BlockNotAcceptedException("ats are not matching at block height " + dp.blockchain.height + " (" + e + ")")
        }

        calculatedRemainingAmount += atBlock.totalAmount
        calculatedRemainingFee += atBlock.totalFees
        // ATs
        if (dp.subscriptionService.isEnabled()) {
            calculatedRemainingFee += dp.subscriptionService.applyUnconfirmed(block.timestamp)
        }
        if (remainingAmount != null && remainingAmount != calculatedRemainingAmount) {
            throw BlockchainProcessor.BlockNotAcceptedException("Calculated remaining amount doesn't add up for block " + block.height)
        }
        if (remainingFee != null && remainingFee != calculatedRemainingFee) {
            throw BlockchainProcessor.BlockNotAcceptedException("Calculated remaining fee doesn't add up for block " + block.height)
        }
        blockListeners.accept(BlockchainProcessor.Event.BEFORE_BLOCK_APPLY, block)
        dp.blockService.apply(block)
        dp.subscriptionService.applyConfirmed(block, dp.blockchain.height)
        if (dp.escrowService.isEnabled()) {
            dp.escrowService.updateOnBlock(block, dp.blockchain.height)
        }
        blockListeners.accept(BlockchainProcessor.Event.AFTER_BLOCK_APPLY, block)
        if (!block.transactions.isEmpty()) {
            dp.transactionProcessor.notifyListeners(block.transactions, TransactionProcessor.Event.ADDED_CONFIRMED_TRANSACTIONS)
        }
    }

    private fun popOffTo(commonBlock: Block): MutableList<Block> {
        require(commonBlock.height >= minRollbackHeight) { "Rollback to height " + commonBlock.height + " not suppported, " + "current height " + dp.blockchain.height }
        if (!dp.blockchain.hasBlock(commonBlock.id)) {
            logger.safeDebug { "Block ${commonBlock.stringId} not found in blockchain, nothing to pop off" }
            return mutableListOf()
        }
        val poppedOffBlocks = mutableListOf<Block>()
        dp.downloadCache.mutex.withLock {
            processMutex.withLock {
                try {
                    dp.db.beginTransaction()
                    var block = dp.blockchain.lastBlock
                    logger.safeInfo { "Rollback from ${block.height} to ${commonBlock.height}" }
                    while (block.id != commonBlock.id && block.id != Genesis.GENESIS_BLOCK_ID) {
                        poppedOffBlocks.add(block)
                        block = popLastBlock()
                    }
                    dp.derivedTableManager.derivedTables.forEach { table -> table.rollback(commonBlock.height) }
                    dp.dbCacheManager.flushCache()
                    dp.db.commitTransaction()
                    dp.downloadCache.resetCache()
                } catch (e: RuntimeException) {
                    dp.db.rollbackTransaction()
                    logger.safeDebug(e) { "Error popping off to ${commonBlock.height}" }
                    throw e
                } finally {
                    dp.db.endTransaction()
                }
            }
        }
        return poppedOffBlocks
    }

    private fun popLastBlock(): Block {
        val block = dp.blockchain.lastBlock
        if (block.id == Genesis.GENESIS_BLOCK_ID) {
            throw RuntimeException("Cannot pop off genesis block")
        }
        val previousBlock = dp.blockDb.findBlock(block.previousBlockId)!!
        dp.blockchain.setLastBlock(block, previousBlock)
        block.transactions.forEach { it.unsetBlock() }
        dp.blockDb.deleteBlocksFrom(block.id)
        blockListeners.accept(BlockchainProcessor.Event.BLOCK_POPPED, block)
        return previousBlock
    }

    private fun preCheckUnconfirmedTransaction(transactionDuplicatesChecker: TransactionDuplicatesCheckerImpl, unconfirmedTransactionStore: UnconfirmedTransactionStore, transaction: Transaction): Boolean {
        val ok = (hasAllReferencedTransactions(transaction, transaction.timestamp, 0)
                && !transactionDuplicatesChecker.hasAnyDuplicate(transaction)
                && !dp.transactionDb.hasTransaction(transaction.id))
        if (!ok) unconfirmedTransactionStore.remove(transaction)
        return ok
    }

    override fun generateBlock(secretPhrase: String, publicKey: ByteArray, nonce: Long?) {
        dp.downloadCache.mutex.withLock {
            dp.downloadCache.lockCache() //stop all incoming blocks.
            val unconfirmedTransactionStore = dp.unconfirmedTransactionStore
            val orderedBlockTransactions = TreeSet<Transaction>()

            var blockSize = dp.fluxCapacitor.getValue(FluxValues.MAX_NUMBER_TRANSACTIONS)
            var payloadSize = dp.fluxCapacitor.getValue(FluxValues.MAX_PAYLOAD_LENGTH)

            var totalAmountNQT: Long = 0
            var totalFeeNQT: Long = 0

            val previousBlock = dp.blockchain.lastBlock
            val blockTimestamp = dp.timeService.epochTime

            // this is just an validation. which collects all valid transactions, which fit into the block
            // finally all stuff is reverted so nothing is written to the db
            // the block itself with all transactions we found is pushed using pushBlock which calls
            // accept (so it's going the same way like a received/synced block)
            try {
                dp.db.beginTransaction()
                val transactionDuplicatesChecker = TransactionDuplicatesCheckerImpl()

                val priorityCalculator = { transaction: Transaction ->
                    var age = blockTimestamp + 1 - transaction.timestamp
                    if (age < 0) age = 1
                    age.toLong() * transaction.feeNQT
                }

                // Map of slot number -> transaction
                val transactionsToBeIncluded: Map<Long, Transaction>
                val inclusionCandidates = unconfirmedTransactionStore.all
                        .filter { transaction -> // Normal filtering
                            transaction.version.toInt() == dp.transactionProcessor.getTransactionVersion(previousBlock.height)
                                    && transaction.expiration >= blockTimestamp
                                    && transaction.timestamp <= blockTimestamp + MAX_TIMESTAMP_DIFFERENCE
                                    && (!dp.fluxCapacitor.getValue(FluxValues.AUTOMATED_TRANSACTION_BLOCK) || dp.economicClustering.verifyFork(transaction))
                        }
                        .filter { transaction -> preCheckUnconfirmedTransaction(transactionDuplicatesChecker, unconfirmedTransactionStore, transaction) } // Extra check for transactions that are to be considered

                if (dp.fluxCapacitor.getValue(FluxValues.PRE_DYMAXION)) {
                    // In this step we get all unconfirmed transactions and then sort them by slot, followed by priority
                    val unconfirmedTransactionsOrderedBySlotThenPriority = mutableMapOf<Long, MutableMap<Long, Transaction>>()
                    inclusionCandidates.associateBy({ it }, priorityCalculator).forEach { (transaction, priority) ->
                        val slot = (transaction.feeNQT - transaction.feeNQT % FEE_QUANT) / FEE_QUANT
                        unconfirmedTransactionsOrderedBySlotThenPriority.computeIfAbsent(slot) { mutableMapOf() }
                        unconfirmedTransactionsOrderedBySlotThenPriority[slot]!![priority] = transaction
                    }

                    // In this step we sort through each slot and find the highest priority transaction in each.
                    val highestSlot = AtomicLong()
                    unconfirmedTransactionsOrderedBySlotThenPriority.keys
                            .forEach { slot ->
                                if (highestSlot.get() < slot) {
                                    highestSlot.set(slot)
                                }
                            }
                    val slotsWithNoTransactions = mutableListOf<Long>()
                    for (slot in 1..highestSlot.get()) {
                        val transactions = unconfirmedTransactionsOrderedBySlotThenPriority[slot]
                        if (transactions.isNullOrEmpty()) {
                            slotsWithNoTransactions.add(slot)
                        }
                    }
                    val unconfirmedTransactionsOrderedBySlot = mutableMapOf<Long, Transaction>()
                    unconfirmedTransactionsOrderedBySlotThenPriority.forEach { (slot, transactions) ->
                        var highestPriority = 0L
                        transactions.keys.forEach { priority ->
                            if (highestPriority < priority) {
                                highestPriority = priority
                            }
                        }
                        unconfirmedTransactionsOrderedBySlot[slot] = transactions[highestPriority]!!
                        transactions.remove(highestPriority) // This is to help with filling slots with no transactions
                    }

                    // If a slot does not have any transactions in it, the next highest priority transaction from the slot above should be used.
                    slotsWithNoTransactions.sortedWith(Comparator.reverseOrder())
                    slotsWithNoTransactions.forEach { emptySlot ->
                        var slotNumberToTakeFrom = emptySlot
                        var slotToTakeFrom: MutableMap<Long, Transaction>? = null
                        while (slotToTakeFrom.isNullOrEmpty()) {
                            slotNumberToTakeFrom++
                            if (slotNumberToTakeFrom > highestSlot.get()) return@forEach
                            slotToTakeFrom = unconfirmedTransactionsOrderedBySlotThenPriority[slotNumberToTakeFrom]
                        }
                        var highestPriority = 0L
                        slotToTakeFrom.keys.forEach { priority ->
                            if (highestPriority < priority) {
                                highestPriority = priority
                            }
                        }
                        unconfirmedTransactionsOrderedBySlot[emptySlot] = slotToTakeFrom[highestPriority]!!
                        slotToTakeFrom.remove(highestPriority)
                    }
                    transactionsToBeIncluded = unconfirmedTransactionsOrderedBySlot
                } else { // Before Pre-Dymaxion HF, just choose highest priority
                    val transactionsOrderedByPriority = inclusionCandidates.associateBy(priorityCalculator, { it })
                    val transactionsOrderedBySlot = mutableMapOf<Long, Transaction>()
                    val currentSlot = AtomicLong(1)
                    transactionsOrderedByPriority.keys
                            .sortedWith(Comparator.reverseOrder())
                            .forEach { priority -> // This should do highest priority to lowest priority
                                transactionsOrderedBySlot[currentSlot.get()] = transactionsOrderedByPriority[priority]!!
                                currentSlot.incrementAndGet()
                            }
                    transactionsToBeIncluded = transactionsOrderedBySlot
                }

                for ((slot, transaction) in transactionsToBeIncluded) {

                    if (blockSize <= 0 || payloadSize <= 0) {
                        break
                    } else if (transaction.size > payloadSize) {
                        continue
                    }

                    val slotFee = if (dp.fluxCapacitor.getValue(FluxValues.PRE_DYMAXION)) slot * FEE_QUANT else ONE_BURST
                    if (transaction.feeNQT >= slotFee) {
                        if (dp.transactionService.applyUnconfirmed(transaction)) {
                            try {
                                dp.transactionService.validate(transaction)
                                payloadSize -= transaction.size
                                totalAmountNQT += transaction.amountNQT
                                totalFeeNQT += transaction.feeNQT
                                orderedBlockTransactions.add(transaction)
                                blockSize--
                            } catch (e: BurstException.NotCurrentlyValidException) {
                                dp.transactionService.undoUnconfirmed(transaction)
                            } catch (e: BurstException.ValidationException) {
                                unconfirmedTransactionStore.remove(transaction)
                                dp.transactionService.undoUnconfirmed(transaction)
                            }

                        } else {
                            // Drop duplicates and transactions that cannot be applied
                            unconfirmedTransactionStore.remove(transaction)
                        }
                    }
                }

                if (dp.subscriptionService.isEnabled()) {
                    dp.subscriptionService.clearRemovals()
                    totalFeeNQT += dp.subscriptionService.calculateFees(blockTimestamp)
                }
            } catch (e: Exception) {
                dp.db.rollbackTransaction()
                throw e
            } finally {
                dp.db.rollbackTransaction() // TODO ????
                dp.db.endTransaction()
            }

            // ATs for block
            AT.clearPendingFees()
            AT.clearPendingTransactions()
            val atBlock = dp.atController.getCurrentBlockATs(payloadSize, previousBlock.height + 1)
            val byteATs = atBlock.bytesForBlock

            // digesting AT Bytes
            if (byteATs != null) {
                payloadSize -= byteATs.size
                totalFeeNQT += atBlock.totalFees
                totalAmountNQT += atBlock.totalAmount
            }

            // ATs for block

            val digest = Crypto.sha256()
            orderedBlockTransactions.forEach { transaction -> digest.update(transaction.toBytes()) }
            val payloadHash = digest.digest()
            val generationSignature = dp.generator.calculateGenerationSignature(
                    previousBlock.generationSignature, previousBlock.generatorId)
            val block: Block
            val previousBlockHash = Crypto.sha256().digest(previousBlock.toBytes())
            try {
                block = Block(dp, blockVersion, blockTimestamp,
                        previousBlock.id, totalAmountNQT, totalFeeNQT, dp.fluxCapacitor.getValue(FluxValues.MAX_PAYLOAD_LENGTH) - payloadSize, payloadHash, publicKey,
                        generationSignature, null, previousBlockHash, orderedBlockTransactions, nonce!!,
                        byteATs, previousBlock.height)
            } catch (e: BurstException.ValidationException) {
                // shouldn't happen because all transactions are already validated
                logger.safeInfo(e) { "Error generating block" }
                return@withLock
            }

            block.sign(secretPhrase)
            dp.blockService.setPrevious(block, previousBlock)
            try {
                dp.blockService.preVerify(block)
                pushBlock(block)
                blockListeners.accept(BlockchainProcessor.Event.BLOCK_GENERATED, block)
                logger.safeDebug { "Account ${block.generatorId.toUnsignedString()} generated block ${block.stringId} at height ${block.height}" }
                dp.downloadCache.resetCache()
            } catch (e: BlockchainProcessor.TransactionNotAcceptedException) {
                logger.safeDebug { "Generate block failed: ${e.message}" }
                val transaction = e.transaction
                logger.safeDebug { "Removing invalid transaction: ${transaction.stringId}" }
                unconfirmedTransactionStore.remove(transaction)
                throw e
            } catch (e: BlockchainProcessor.BlockNotAcceptedException) {
                logger.safeDebug { "Generate block failed: ${e.message}" }
                throw e
            }

        }
    }

    private fun hasAllReferencedTransactions(transaction: Transaction, timestamp: Int, count: Int): Boolean {
        if (transaction.referencedTransactionFullHash == null) {
            return timestamp - transaction.timestamp < 60 * 1440 * 60 && count < 10
        }
        val foundTransaction = dp.transactionDb.findTransactionByFullHash(transaction.referencedTransactionFullHash)
        if (!dp.subscriptionService.isEnabled() && foundTransaction != null && transaction.signature == null) {
            return false
        }
        return foundTransaction != null && hasAllReferencedTransactions(foundTransaction, timestamp, count + 1)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BlockchainProcessorImpl::class.java)

        private const val MAX_TIMESTAMP_DIFFERENCE = 15
    }
}
