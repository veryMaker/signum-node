package brs

import brs.Constants.FEE_QUANT
import brs.Constants.ONE_BURST
import brs.at.AT
import brs.at.AtBlock
import brs.at.AtException
import brs.crypto.Crypto
import brs.db.sql.Db
import brs.fluxcapacitor.FluxValues
import brs.peer.Peer
import brs.props.Props
import brs.transactionduplicates.TransactionDuplicatesCheckerImpl
import brs.unconfirmedtransactions.UnconfirmedTransactionStore
import brs.util.*
import brs.util.delegates.Atomic
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max
import kotlin.system.exitProcess

class BlockchainProcessorImpl(private val dp: DependencyProvider) : BlockchainProcessor {

    private val logger = LoggerFactory.getLogger(BlockchainProcessorImpl::class.java)
    override val oclVerify = dp.propertyService.get(Props.GPU_ACCELERATION)
    private val oclUnverifiedQueue = dp.propertyService.get(Props.GPU_UNVERIFIED_QUEUE)

    private val gpuUsage = Semaphore(2)

    private val trimDerivedTables = dp.propertyService.get(Props.DB_TRIM_DERIVED_TABLES)
    private val lastTrimHeight = AtomicInteger()

    private val blockListeners = Listeners<Block, BlockchainProcessor.Event>()
    override var lastBlockchainFeeder by Atomic<Peer?>()
    override var lastBlockchainFeederHeight by Atomic<Int?>()
    private val getMoreBlocks = AtomicBoolean(true)

    override val isScanning by Atomic(false)

    private val autoPopOffEnabled = dp.propertyService.get(Props.AUTO_POP_OFF_ENABLED)
    private var autoPopOffLastStuckHeight = 0
    private var autoPopOffNumberOfBlocks = 0

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

        // use GPU acceleration ?

        blockListeners.addListener({ block ->
            if (block.height % 5000 == 0) {
                logger.info("processed block {}", block.height)
            }
        }, BlockchainProcessor.Event.BLOCK_SCANNED)

        blockListeners.addListener({ block ->
            if (block.height % 5000 == 0) {
                logger.info("processed block {}", block.height)
            }
        }, BlockchainProcessor.Event.BLOCK_PUSHED)

        blockListeners.addListener({ block -> dp.transactionProcessor.revalidateUnconfirmedTransactions() }, BlockchainProcessor.Event.BLOCK_PUSHED)

        if (trimDerivedTables) {
            blockListeners.addListener({ block ->
                if (block.height % 1440 == 0) {
                    lastTrimHeight.set(Math.max(block.height - dp.propertyService.get(Props.DB_MAX_ROLLBACK), 0))
                    if (lastTrimHeight.get() > 0) {
                        dp.derivedTableManager.derivedTables.forEach { table -> table.trim(lastTrimHeight.get()) }
                    }
                }
            }, BlockchainProcessor.Event.AFTER_BLOCK_APPLY)
        }

        addGenesisBlock()

        val getMoreBlocksThread = object : () -> Unit { // TODO refactor these
            private val getCumulativeDifficultyRequest by lazy {
                val request = JsonObject()
                request.addProperty("requestType", "getCumulativeDifficulty")
                JSON.prepareRequest(request)
            }

            private var peerHasMore: Boolean = false

            override fun invoke() {
                if (dp.propertyService.get(Props.DEV_OFFLINE)) return
                while (!Thread.currentThread().isInterrupted && ThreadPool.running.get()) {
                    try {
                        try {
                            if (!getMoreBlocks.get()) {
                                return
                            }
                            // unlocking cache for writing.
                            // This must be done before we query where to add blocks.
                            // We sync the cache in event of pop off
                            synchronized(dp.downloadCache) {
                                dp.downloadCache.unlockCache()
                            }


                            if (dp.downloadCache.isFull) {
                                return
                            }
                            peerHasMore = true
                            val peer = dp.peers.getAnyPeer(Peer.State.CONNECTED)
                            if (peer == null) {
                                logger.debug("No peer connected.")
                                return
                            }
                            val response = peer.send(getCumulativeDifficultyRequest) ?: return
                            if (response["blockchainHeight"] != null) {
                                lastBlockchainFeeder = peer
                                lastBlockchainFeederHeight = JSON.getAsInt(response["blockchainHeight"])
                            } else {
                                logger.debug("Peer has no chainheight")
                                return
                            }

                            /* Cache now contains Cumulative Difficulty */

                            val curCumulativeDifficulty = dp.downloadCache.cumulativeDifficulty
                            val peerCumulativeDifficulty = JSON.getAsString(response.get("cumulativeDifficulty"))
                            if (peerCumulativeDifficulty.isEmpty()) {
                                logger.debug("Peer CumulativeDifficulty is null")
                                return
                            }
                            val betterCumulativeDifficulty = BigInteger(peerCumulativeDifficulty)
                            if (betterCumulativeDifficulty <= curCumulativeDifficulty) {
                                return
                            }

                            var commonBlockId = Genesis.GENESIS_BLOCK_ID
                            val cacheLastBlockId = dp.downloadCache.getLastBlockId()

                            // Now we will find the highest common block between ourself and our peer
                            if (cacheLastBlockId != Genesis.GENESIS_BLOCK_ID) {
                                commonBlockId = getCommonMilestoneBlockId(peer)
                                if (commonBlockId == 0L || !peerHasMore) {
                                    logger.debug("We could not get a common milestone block from peer.")
                                    return
                                }
                            }

                            /*
               * if we did not get the last block in chain as common block we will be downloading a
               * fork. however if it is to far off we cannot process it anyway. canBeFork will check
               * where in chain this common block is fitting and return true if it is worth to
               * continue.
               */

                            var saveInCache = true
                            if (commonBlockId != cacheLastBlockId) {
                                if (dp.downloadCache.canBeFork(commonBlockId)) {
                                    // the fork is not that old. Lets see if we can get more precise.
                                    commonBlockId = getCommonBlockId(peer, commonBlockId)
                                    if (commonBlockId == 0L || !peerHasMore) {
                                        logger.debug("Trying to get a more precise common block resulted in an error.")
                                        return
                                    }
                                    saveInCache = false
                                    dp.downloadCache.resetForkBlocks()
                                } else {
                                    if (logger.isWarnEnabled) {
                                        logger.warn("Our peer want to feed us a fork that is more than {} blocks old.", dp.propertyService.get(Props.DB_MAX_ROLLBACK))
                                    }
                                    return
                                }
                            }

                            val nextBlocks = getNextBlocks(peer, commonBlockId)
                            if (nextBlocks == null || nextBlocks.size() == 0) {
                                logger.debug("Peer did not feed us any blocks")
                                return
                            }

                            // download blocks from peer
                            var lastBlock = dp.downloadCache.getBlock(commonBlockId)
                            if (lastBlock == null) {
                                logger.info("Error: lastBlock is null")
                                return
                            }

                            // loop blocks and make sure they fit in chain
                            var block: Block
                            var blockData: JsonObject

                            for (o in nextBlocks) {
                                val height = lastBlock!!.height + 1
                                blockData = JSON.getAsJsonObject(o)
                                try {
                                    block = Block.parseBlock(dp, blockData, height)
                                    // Make sure it maps back to chain
                                    if (lastBlock.id != block.previousBlockId) {
                                        logger.debug("Discarding downloaded data. Last downloaded blocks is rubbish")
                                        logger.debug("DB blockID: {} DB blockheight: {} Downloaded previd: {}", lastBlock.id, lastBlock.height, block.previousBlockId)
                                        return
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
                                                return
                                            }
                                            if (logger.isDebugEnabled) {
                                                logger.debug("Added from download: Id: {} Height: {}", block.id, block.height)
                                            }
                                        }
                                    } else {
                                        dp.downloadCache.addForkBlock(block)
                                    }
                                    lastBlock = block
                                } catch (e: BlockchainProcessor.BlockOutOfOrderException) {
                                    logger.info("$e - autoflushing cache to get rid of it", e)
                                    dp.downloadCache.resetCache()
                                    return
                                } catch (e: RuntimeException) {
                                    logger.info("Failed to parse block: {}$e", e)
                                    logger.info("Failed to parse block trace: {}", Arrays.toString(e.stackTrace))
                                    peer.blacklist(e, "pulled invalid data using getCumulativeDifficulty")
                                    return
                                } catch (e: BurstException.ValidationException) {
                                    logger.info("Failed to parse block: {}$e", e)
                                    logger.info("Failed to parse block trace: {}", Arrays.toString(e.stackTrace))
                                    peer.blacklist(e, "pulled invalid data using getCumulativeDifficulty")
                                    return
                                } catch (e: Exception) {
                                    logger.warn("Unhandled exception {}$e", e)
                                    logger.warn("Unhandled exception trace: {}", Arrays.toString(e.stackTrace))
                                }

                                //executor shutdown?
                                if (Thread.currentThread().isInterrupted)
                                    return
                            } // end block loop

                            if (logger.isTraceEnabled) {
                                logger.trace("Unverified blocks: {}", dp.downloadCache.unverifiedSize)
                                logger.trace("Blocks in cache: {}", dp.downloadCache.size())
                                logger.trace("Bytes in cache: {}", dp.downloadCache.blockCacheSize)
                            }
                            if (!saveInCache) {
                                /*
                                 * Since we cannot rely on peers reported cumulative difficulty we do
                                 * a final check to see that the CumulativeDifficulty actually is bigger
                                 * before we do a popOff and switch chain.
                                 */
                                if (lastBlock!!.cumulativeDifficulty < curCumulativeDifficulty) {
                                    peer.blacklist("peer claimed to have bigger cumulative difficulty but in reality it did not.")
                                    dp.downloadCache.resetForkBlocks()
                                    break
                                }
                                processFork(peer, dp.downloadCache.forkList, commonBlockId)
                            }

                        } catch (e: BurstException.StopException) {
                            logger.info("Blockchain download stopped: {}", e.message)
                        } catch (e: Exception) {
                            logger.info("Error in blockchain download thread", e)
                        }
                        // end second try
                    } catch (t: Exception) {
                        logger.info("CRITICAL ERROR. PLEASE REPORT TO THE DEVELOPERS.\n$t", t)
                        exitProcess(1)
                    }
                }
            }

            private fun getCommonMilestoneBlockId(peer: Peer?): Long {

                var lastMilestoneBlockId: String? = null

                while (!Thread.currentThread().isInterrupted && ThreadPool.running.get()) {
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
                        logger.debug("Got null response in getCommonMilestoneBlockId")
                        return 0
                    }
                    val milestoneBlockIds = JSON.getAsJsonArray(response.get("milestoneBlockIds"))
                    if (milestoneBlockIds.isEmpty()) {
                        logger.debug("MilestoneArray is empty")
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
                    if (JSON.getAsBoolean(response.get("last"))) {
                        peerHasMore = false
                    }

                    for (milestoneBlockId in milestoneBlockIds) {
                        val blockId = JSON.getAsString(milestoneBlockId).parseUnsignedLong()

                        if (dp.downloadCache.hasBlock(blockId)) {
                            if (lastMilestoneBlockId == null && milestoneBlockIds.size() > 1) {
                                peerHasMore = false
                                logger.debug("Peer dont have more (cache)")
                            }
                            return blockId
                        }
                        lastMilestoneBlockId = JSON.getAsString(milestoneBlockId)
                    }
                }
                throw InterruptedException("interrupted")
            }

            private fun getCommonBlockId(peer: Peer?, commonBlockId: Long): Long {
                var commonBlockId = commonBlockId

                while (!Thread.currentThread().isInterrupted && ThreadPool.running.get()) {
                    val request = JsonObject()
                    request.addProperty("requestType", "getNextBlockIds")
                    request.addProperty("blockId", commonBlockId.toUnsignedString())
                    val response = peer!!.send(JSON.prepareRequest(request)) ?: return 0
                    val nextBlockIds = JSON.getAsJsonArray(response.get("nextBlockIds"))
                    if (nextBlockIds == null || nextBlockIds.size() == 0) {
                        return 0
                    }
                    // prevent overloading with blockIds
                    if (nextBlockIds.size() > 1440) {
                        peer.blacklist("obsolete or rogue peer sends too many nextBlocks")
                        return 0
                    }

                    for (nextBlockId in nextBlockIds) {
                        val blockId = JSON.getAsString(nextBlockId).parseUnsignedLong()
                        if (!dp.downloadCache.hasBlock(blockId)) {
                            return commonBlockId
                        }
                        commonBlockId = blockId
                    }
                }

                throw InterruptedException("interrupted")
            }

            private fun getNextBlocks(peer: Peer?, curBlockId: Long): JsonArray? {

                val request = JsonObject()
                request.addProperty("requestType", "getNextBlocks")
                request.addProperty("blockId", curBlockId.toUnsignedString())
                if (logger.isDebugEnabled) {
                    logger.debug("Getting next Blocks after {} from {}", curBlockId, peer!!.peerAddress)
                }
                val response = peer!!.send(JSON.prepareRequest(request)) ?: return null

                val nextBlocks = JSON.getAsJsonArray(response.get("nextBlocks")) ?: return null
// prevent overloading with blocks
                if (nextBlocks.size() > 1440) {
                    peer.blacklist("obsolete or rogue peer sends too many nextBlocks")
                    return null
                }
                logger.debug("Got {} blocks after {} from {}", nextBlocks.size(), curBlockId, peer.peerAddress)
                return nextBlocks

            }

            private fun processFork(peer: Peer?, forkBlocks: List<Block>, forkBlockId: Long) {
                logger.warn("A fork is detected. Waiting for cache to be processed.")
                dp.downloadCache.lockCache() //dont let anything add to cache!
                while (!Thread.currentThread().isInterrupted && ThreadPool.running.get()) {
                    if (dp.downloadCache.size() == 0) {
                        break
                    }
                    try {
                        Thread.sleep(1000)
                    } catch (ex: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }

                }
                synchronized(dp.downloadCache) {
                    synchronized(dp.transactionProcessor.getUnconfirmedTransactionsSyncObj()) {
                        logger.warn("Cache is now processed. Starting to process fork.")
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
                                    } catch (e: InterruptedException) {
                                        Thread.currentThread().interrupt()
                                    } catch (e: BlockchainProcessor.BlockNotAcceptedException) {
                                        peer!!.blacklist(e, "during processing a fork")
                                        break
                                    }

                                }
                            }
                        }

                        /*
             * we check if we succeeded to push any block. if we did we check against cumulative
             * difficulty If it is lower we blacklist peer and set chain to be processed later.
             */
                        if (pushedForkBlocks > 0 && dp.blockchain.lastBlock.cumulativeDifficulty
                                        .compareTo(curCumulativeDifficulty) < 0) {
                            logger.warn("Fork was bad and pop off was caused by peer {}, blacklisting", peer!!.peerAddress)
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
                                } catch (e: InterruptedException) {
                                    Thread.currentThread().interrupt()
                                } catch (e: BlockchainProcessor.BlockNotAcceptedException) {
                                    logger.warn("Popped off block no longer acceptable: " + block.jsonObject.toJsonString(), e)
                                    break
                                }

                            }
                        } else {
                            myPoppedOffBlocks.forEach { block -> dp.transactionProcessor.processLater(block.transactions) }
                            logger.warn("Successfully switched to better chain.")
                        }
                        logger.warn("Forkprocessing complete.")
                        dp.downloadCache.resetForkBlocks()
                        dp.downloadCache.resetCache() // Reset and set cached vars to chaindata.
                    }
                }
            }
        }
        dp.threadPool.scheduleThread("GetMoreBlocks", getMoreBlocksThread, Constants.BLOCK_PROCESS_THREAD_DELAY, TimeUnit.MILLISECONDS)
        /* this should fetch first block in cache */
        //resetting cache because we have blocks that cannot be processed.
        //pushblock removes the block from cache.
        val blockImporterThread = {
            while (!Thread.interrupted() && ThreadPool.running.get() && dp.downloadCache.size() > 0) {
                try {
                    val lastBlock = dp.blockchain.lastBlock
                    val lastId = lastBlock.id
                    val currentBlock = dp.downloadCache.getNextBlock(lastId) /* this should fetch first block in cache */
                    if (currentBlock == null || currentBlock.height != lastBlock.height + 1) {
                        if (logger.isDebugEnabled) {
                            logger.debug("cache is reset due to orphaned block(s). CacheSize: {}", dp.downloadCache.size())
                        }
                        dp.downloadCache.resetCache() //resetting cache because we have blocks that cannot be processed.
                        break
                    }
                    try {
                        if (!currentBlock.isVerified) {
                            dp.downloadCache.removeUnverified(currentBlock.id)
                            dp.blockService.preVerify(currentBlock)
                            logger.debug("block was not preverified")
                        }
                        pushBlock(currentBlock) //pushblock removes the block from cache.
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                    } catch (e: BlockchainProcessor.BlockNotAcceptedException) {
                        logger.error("Block not accepted", e)
                        blacklistClean(currentBlock, e, "found invalid pull/push data during importing the block")
                        autoPopOff(currentBlock.height)
                        break
                    }

                } catch (exception: Exception) {
                    logger.error("Uncaught exception in blockImporterThread", exception)
                }

            }
        }
        dp.threadPool.scheduleThread("ImportBlocks", blockImporterThread, Constants.BLOCK_PROCESS_THREAD_DELAY, TimeUnit.MILLISECONDS)
        //Is there anything to verify
        //should we use Ocl?
        //is Ocl ready ?
        //verify using java
        val pocVerificationThread = {
            var verifyWithOcl: Boolean
            val queueThreshold = if (oclVerify) oclUnverifiedQueue else 0

            while (!Thread.interrupted() && ThreadPool.running.get()) {
                try {
                    Thread.sleep(10)
                } catch (ex: InterruptedException) {
                    Thread.currentThread().interrupt()
                }

                val unVerified = dp.downloadCache.unverifiedSize
                if (unVerified > queueThreshold) { //Is there anything to verify
                    if (unVerified >= oclUnverifiedQueue && oclVerify) { //should we use Ocl?
                        verifyWithOcl = true
                        if (!gpuUsage.tryAcquire()) { //is Ocl ready ?
                            logger.debug("already max locked")
                            verifyWithOcl = false
                        }
                    } else {
                        verifyWithOcl = false
                    }
                    if (verifyWithOcl) {
                        val pocVersion = dp.downloadCache.getPoCVersion(dp.downloadCache.getUnverifiedBlockIdFromPos(0))
                        var pos = 0
                        val blocks = LinkedList<Block>()
                        while (!Thread.interrupted() && ThreadPool.running.get() && dp.downloadCache.unverifiedSize - 1 > pos && blocks.size < dp.oclPoC.maxItems) {
                            val blockId = dp.downloadCache.getUnverifiedBlockIdFromPos(pos)
                            if (dp.downloadCache.getPoCVersion(blockId) != pocVersion) {
                                break
                            }
                            blocks.add(dp.downloadCache.getBlock(blockId)!!)
                            pos += 1
                        }
                        try {
                            dp.oclPoC.validatePoC(blocks, pocVersion, dp.blockService)
                            dp.downloadCache.removeUnverifiedBatch(blocks)
                        } catch (e: OCLPoC.PreValidateFailException) {
                            logger.info(e.toString(), e)
                            blacklistClean(e.block, e, "found invalid pull/push data during processing the pocVerification")
                        } catch (e: OCLPoC.OCLCheckerException) {
                            logger.info("Open CL error. slow verify will occur for the next $oclUnverifiedQueue Blocks", e)
                        } catch (e: Exception) {
                            logger.info("Unspecified Open CL error: ", e)
                        } finally {
                            gpuUsage.release()
                        }
                    } else { //verify using java
                        try {
                            if (dp.downloadCache.firstUnverifiedBlock != null) {
                                dp.blockService.preVerify(dp.downloadCache.firstUnverifiedBlock!!)
                            }
                        } catch (e: InterruptedException) {
                            Thread.currentThread().interrupt()
                        } catch (e: BlockchainProcessor.BlockNotAcceptedException) {
                            logger.error("Block failed to preverify: ", e)
                        }

                    }
                }
            }
        }
        if (dp.propertyService.get(Props.GPU_ACCELERATION)) {
            logger.debug("Starting preverifier thread in Open CL mode.")
            dp.threadPool.scheduleThread("VerifyPoc", pocVerificationThread, Constants.BLOCK_PROCESS_THREAD_DELAY, TimeUnit.MILLISECONDS)
        } else {
            logger.debug("Starting preverifier thread in CPU mode.")
            dp.threadPool.scheduleThreadCores(pocVerificationThread, Constants.BLOCK_PROCESS_THREAD_DELAY, TimeUnit.MILLISECONDS)
        }
    }

    private fun blacklistClean(block: Block?, e: Exception, description: String) {
        logger.debug("Blacklisting peer and cleaning cache queue")
        if (block == null) {
            return
        }
        val peer = block.peer
        peer?.blacklist(e, description)
        dp.downloadCache.resetCache()
        logger.debug("Blacklisted peer and cleaned queue")
    }

    private fun autoPopOff(height: Int) {
        if (!autoPopOffEnabled) {
            logger.warn("Not automatically popping off as it is disabled via properties. If your node becomes stuck you will need to manually pop off.")
            return
        }
        synchronized(dp.transactionProcessor.getUnconfirmedTransactionsSyncObj()) {
            logger.warn("Auto popping off as failed to push block")
            if (height != autoPopOffLastStuckHeight) {
                autoPopOffLastStuckHeight = height
                autoPopOffNumberOfBlocks = 0
            }
            if (autoPopOffNumberOfBlocks == 0) {
                logger.warn("Not popping anything off as this was the first failure at this height")
            } else {
                logger.warn("Popping off {} blocks due to previous failures to push this block", autoPopOffNumberOfBlocks)
                popOffTo(dp.blockchain.height - autoPopOffNumberOfBlocks)
            }
            autoPopOffNumberOfBlocks++
        }
    }

    override fun addListener(listener: (Block) -> Unit, eventType: BlockchainProcessor.Event): Boolean {
        return blockListeners.addListener(listener, eventType)
    }

    override fun removeListener(listener: (Block) -> Unit, eventType: BlockchainProcessor.Event): Boolean {
        return blockListeners.removeListener(listener, eventType)
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
            logger.debug("Peer {} added block from Announce: Id: {} Height: {}", peer.peerAddress, newBlock.id, newBlock.height)
        } else {
            logger.debug("Peer {} sent us block: {} which is not the follow-up block for {}", peer.peerAddress, newBlock.previousBlockId, chainblock.id)
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

    internal fun setGetMoreBlocks(getMoreBlocks: Boolean) {
        this.getMoreBlocks.set(getMoreBlocks)
    }

    private fun addBlock(block: Block) {
        dp.blockchainStore.addBlock(block)
        dp.blockchain.lastBlock = block
    }

    private fun addGenesisBlock() {
        if (dp.blockDb.hasBlock(Genesis.GENESIS_BLOCK_ID)) {
            logger.info("Genesis block already in database")
            val lastBlock = dp.blockDb.findLastBlock()!!
            dp.blockchain.lastBlock = lastBlock
            logger.info("Last block height: {}", lastBlock.height)
            return
        }
        logger.info("Genesis block not in database, starting from scratch")
        try {
            val genesisBlock = Block(dp, -1, 0, 0, 0, 0, 0,
                    Crypto.sha256().digest(), Genesis.creatorPublicKey, ByteArray(32),
                    Genesis.genesisBlockSignature, null, emptyList(), 0, byteArrayOf(), -1)
            dp.blockService.setPrevious(genesisBlock, null)
            addBlock(genesisBlock)
        } catch (e: BurstException.ValidationException) {
            logger.info(e.message)
            throw RuntimeException(e.toString(), e)
        }

    }

    private fun pushBlock(block: Block) {
        synchronized(dp.transactionProcessor.getUnconfirmedTransactionsSyncObj()) {
            Db.beginTransaction()
            val curTime = dp.timeService.epochTime

            var previousLastBlock: Block? = null
            try {

                previousLastBlock = dp.blockchain.lastBlock

                if (previousLastBlock.id != block.previousBlockId) {
                    throw BlockchainProcessor.BlockOutOfOrderException(
                            "Previous block id doesn't match for block " + block.height
                                    + if (previousLastBlock.height + 1 == block.height) "" else " invalid previous height " + previousLastBlock.height
                    )
                }

                if (block.version != blockVersion) {
                    throw BlockchainProcessor.BlockNotAcceptedException("Invalid version " + block.version + " for block " + block.height)
                }

                if (block.version != 1 && !Arrays.equals(Crypto.sha256().digest(previousLastBlock.bytes),
                                block.previousBlockHash)) {
                    throw BlockchainProcessor.BlockNotAcceptedException("Previous block hash doesn't match for block " + block.height)
                }
                if (block.timestamp > curTime + MAX_TIMESTAMP_DIFFERENCE || block.timestamp <= previousLastBlock.timestamp) {
                    throw BlockchainProcessor.BlockOutOfOrderException("Invalid timestamp: " + block.timestamp
                            + " current time is " + curTime
                            + ", previous block timestamp is " + previousLastBlock.timestamp)
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
                    if (transaction.timestamp > curTime + MAX_TIMESTAMP_DIFFERENCE) {
                        throw BlockchainProcessor.BlockOutOfOrderException("Invalid transaction timestamp: "
                                + transaction.timestamp + ", current time is " + curTime)
                    }
                    if (transaction.timestamp > block.timestamp + MAX_TIMESTAMP_DIFFERENCE || transaction.expiration < block.timestamp) {
                        throw BlockchainProcessor.TransactionNotAcceptedException("Invalid transaction timestamp "
                                + transaction.timestamp + " for transaction " + transaction.stringId
                                + ", current time is " + curTime + ", block timestamp is " + block.timestamp,
                                transaction)
                    }
                    if (dp.transactionDb.hasTransaction(transaction.id)) {
                        throw BlockchainProcessor.TransactionNotAcceptedException(
                                "Transaction " + transaction.stringId + " is already in the blockchain",
                                transaction)
                    }
                    if (transaction.referencedTransactionFullHash != null && !hasAllReferencedTransactions(transaction, transaction.timestamp, 0)) {
                        throw BlockchainProcessor.TransactionNotAcceptedException("Missing or invalid referenced transaction "
                                + transaction.referencedTransactionFullHash + " for transaction "
                                + transaction.stringId, transaction)
                    }
                    if (transaction.version.toInt() != dp.transactionProcessor.getTransactionVersion(previousLastBlock.height)) {
                        throw BlockchainProcessor.TransactionNotAcceptedException("Invalid transaction version "
                                + transaction.version + " at height " + previousLastBlock.height,
                                transaction)
                    }

                    if (!dp.transactionService.verifyPublicKey(transaction)) {
                        throw BlockchainProcessor.TransactionNotAcceptedException("Wrong public key in transaction "
                                + transaction.stringId + " at height " + previousLastBlock.height,
                                transaction)
                    }
                    if (dp.fluxCapacitor.getValue(FluxValues.AUTOMATED_TRANSACTION_BLOCK) && !dp.economicClustering.verifyFork(transaction)) {
                        if (logger.isDebugEnabled) {
                            logger.debug("Block {} height {} contains transaction that was generated on a fork: {} ecBlockId {} ecBlockHeight {}", block.stringId, previousLastBlock.height + 1, transaction.stringId, transaction.ecBlockHeight, transaction.ecBlockId.toUnsignedString())
                        }
                        throw BlockchainProcessor.TransactionNotAcceptedException("Transaction belongs to a different fork",
                                transaction)
                    }
                    if (transaction.id == 0L) {
                        throw BlockchainProcessor.TransactionNotAcceptedException("Invalid transaction id", transaction)
                    }

                    if (transactionDuplicatesChecker.hasAnyDuplicate(transaction)) {
                        throw BlockchainProcessor.TransactionNotAcceptedException("Transaction is a duplicate: " + transaction.stringId, transaction)
                    }

                    try {
                        dp.transactionService.validate(transaction)
                    } catch (e: BurstException.ValidationException) {
                        throw BlockchainProcessor.TransactionNotAcceptedException(e.message!!, transaction)
                    }

                    calculatedTotalAmount += transaction.amountNQT
                    calculatedTotalFee += transaction.feeNQT
                    digest.update(transaction.bytes)
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
                        if (feeArray[i] >= Constants.FEE_QUANT * (i + 1)) {
                            throw BlockchainProcessor.BlockNotAcceptedException("Transaction fee is not enough to be included in this block " + block.height)
                        }
                    }
                }

                if (!Arrays.equals(digest.digest(), block.payloadHash)) {
                    throw BlockchainProcessor.BlockNotAcceptedException("Payload hash doesn't match for block " + block.height)
                }

                val remainingAmount = Convert.safeSubtract(block.totalAmountNQT, calculatedTotalAmount)
                val remainingFee = Convert.safeSubtract(block.totalFeeNQT, calculatedTotalFee)

                dp.blockService.setPrevious(block, previousLastBlock)
                blockListeners.accept(block, BlockchainProcessor.Event.BEFORE_BLOCK_ACCEPT)
                dp.transactionProcessor.removeForgedTransactions(block.transactions)
                dp.transactionProcessor.requeueAllUnconfirmedTransactions()
                dp.accountService.flushAccountTable()
                addBlock(block)
                dp.downloadCache.removeBlock(block) // We make sure downloadCache do not have this block anymore.
                accept(block, remainingAmount, remainingFee)
                dp.derivedTableManager.derivedTables.forEach { it.finish() }
                Db.commitTransaction()
            } catch (e: BlockchainProcessor.BlockNotAcceptedException) {
                Db.rollbackTransaction()
                if (previousLastBlock != null) {
                    dp.blockchain.lastBlock = previousLastBlock
                }
                dp.downloadCache.resetCache()
                throw e
            } catch (e: ArithmeticException) {
                Db.rollbackTransaction()
                if (previousLastBlock != null) {
                    dp.blockchain.lastBlock = previousLastBlock
                }
                dp.downloadCache.resetCache()
                throw e
            } finally {
                Db.endTransaction()
            }
            logger.debug("Successfully pushed {} (height {})", block.id, block.height)
            dp.statisticsManager.blockAdded()
            blockListeners.accept(block, BlockchainProcessor.Event.BLOCK_PUSHED)
            if (block.timestamp >= dp.timeService.epochTime - MAX_TIMESTAMP_DIFFERENCE) {
                dp.peers.sendToSomePeers(block)
            }
            if (block.height >= autoPopOffLastStuckHeight) {
                autoPopOffNumberOfBlocks = 0
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
            // TODO NN Assert might cause problems...
            atBlock = dp.atController.validateATs(block.blockATs!!, dp.blockchain.height)
        } catch (e: AtException) {
            throw BlockchainProcessor.BlockNotAcceptedException("ats are not matching at block height " + dp.blockchain.height + " (" + e + ")")
        }

        calculatedRemainingAmount += atBlock.totalAmount
        calculatedRemainingFee += atBlock.totalFees
        // ATs
        if (dp.subscriptionService.isEnabled) {
            calculatedRemainingFee += dp.subscriptionService.applyUnconfirmed(block.timestamp)
        }
        if (remainingAmount != null && remainingAmount != calculatedRemainingAmount) {
            throw BlockchainProcessor.BlockNotAcceptedException("Calculated remaining amount doesn't add up for block " + block.height)
        }
        if (remainingFee != null && remainingFee != calculatedRemainingFee) {
            throw BlockchainProcessor.BlockNotAcceptedException("Calculated remaining fee doesn't add up for block " + block.height)
        }
        blockListeners.accept(block, BlockchainProcessor.Event.BEFORE_BLOCK_APPLY)
        dp.blockService.apply(block)
        dp.subscriptionService.applyConfirmed(block, dp.blockchain.height)
        if (dp.escrowService.isEnabled) {
            dp.escrowService.updateOnBlock(block, dp.blockchain.height)
        }
        blockListeners.accept(block, BlockchainProcessor.Event.AFTER_BLOCK_APPLY)
        if (!block.transactions.isEmpty()) {
            dp.transactionProcessor.notifyListeners(block.transactions, TransactionProcessor.Event.ADDED_CONFIRMED_TRANSACTIONS)
        }
    }

    private fun popOffTo(commonBlock: Block): MutableList<Block> {
        require(commonBlock.height >= minRollbackHeight) { "Rollback to height " + commonBlock.height + " not suppported, " + "current height " + dp.blockchain.height }
        if (!dp.blockchain.hasBlock(commonBlock.id)) {
            logger.debug("Block {} not found in blockchain, nothing to pop off", commonBlock.stringId)
            return mutableListOf()
        }
        val poppedOffBlocks = mutableListOf<Block>()
        synchronized(dp.downloadCache) {
            synchronized(dp.transactionProcessor.getUnconfirmedTransactionsSyncObj()) {
                try {
                    Db.beginTransaction()
                    var block = dp.blockchain.lastBlock
                    logger.info("Rollback from {} to {}", block.height, commonBlock.height)
                    while (block.id != commonBlock.id && block.id != Genesis.GENESIS_BLOCK_ID) {
                        poppedOffBlocks.add(block)
                        block = popLastBlock()
                    }
                    dp.derivedTableManager.derivedTables.forEach { table -> table.rollback(commonBlock.height) }
                    dp.dbCacheManager.flushCache()
                    Db.commitTransaction()
                    dp.downloadCache.resetCache()
                } catch (e: RuntimeException) {
                    Db.rollbackTransaction()
                    logger.debug("Error popping off to {}", commonBlock.height, e)
                    throw e
                } finally {
                    Db.endTransaction()
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
        blockListeners.accept(block, BlockchainProcessor.Event.BLOCK_POPPED)
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
        synchronized(dp.downloadCache) {
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
                Db.beginTransaction()
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

                if (dp.subscriptionService.isEnabled) {
                    dp.subscriptionService.clearRemovals()
                    totalFeeNQT += dp.subscriptionService.calculateFees(blockTimestamp)
                }
            } catch (e: Exception) {
                Db.rollbackTransaction()
                throw e
            } finally {
                Db.rollbackTransaction() // TODO ????
                Db.endTransaction()
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
            orderedBlockTransactions.forEach { transaction -> digest.update(transaction.bytes) }
            val payloadHash = digest.digest()
            val generationSignature = dp.generator.calculateGenerationSignature(
                    previousBlock.generationSignature, previousBlock.generatorId)
            val block: Block
            val previousBlockHash = Crypto.sha256().digest(previousBlock.bytes)
            try {
                block = Block(dp, blockVersion, blockTimestamp,
                        previousBlock.id, totalAmountNQT, totalFeeNQT, dp.fluxCapacitor.getValue(FluxValues.MAX_PAYLOAD_LENGTH) - payloadSize, payloadHash, publicKey,
                        generationSignature, null, previousBlockHash, orderedBlockTransactions, nonce!!,
                        byteATs, previousBlock.height)
            } catch (e: BurstException.ValidationException) {
                // shouldn't happen because all transactions are already validated
                logger.info("Error generating block", e)
                return
            }

            block.sign(secretPhrase)
            dp.blockService.setPrevious(block, previousBlock)
            try {
                dp.blockService.preVerify(block)
                pushBlock(block)
                blockListeners.accept(block, BlockchainProcessor.Event.BLOCK_GENERATED)
                if (logger.isDebugEnabled) {
                    logger.debug("Account {} generated block {} at height {}", block.generatorId.toUnsignedString(), block.stringId, block.height)
                }
                dp.downloadCache.resetCache()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: BlockchainProcessor.TransactionNotAcceptedException) {
                logger.debug("Generate block failed: {}", e.message)
                val transaction = e.transaction
                logger.debug("Removing invalid transaction: {}", transaction.stringId)
                unconfirmedTransactionStore.remove(transaction)
                throw e
            } catch (e: BlockchainProcessor.BlockNotAcceptedException) {
                logger.debug("Generate block failed: {}", e.message)
                throw e
            }

        } //end synchronized cache
    }

    private fun hasAllReferencedTransactions(transaction: Transaction, timestamp: Int, count: Int): Boolean {
        if (transaction.referencedTransactionFullHash == null) {
            return timestamp - transaction.timestamp < 60 * 1440 * 60 && count < 10
        }
        val foundTransaction = dp.transactionDb.findTransactionByFullHash(transaction.referencedTransactionFullHash)
        if (!dp.subscriptionService.isEnabled && foundTransaction != null && transaction.signature == null) {
            return false
        }
        return foundTransaction != null && hasAllReferencedTransactions(foundTransaction, timestamp, count + 1)
    }

    companion object {
        private const val MAX_TIMESTAMP_DIFFERENCE = 15
    }
}
