package brs.util

import brs.Block
import brs.DependencyProvider
import brs.Genesis
import brs.fluxcapacitor.FluxValues
import brs.props.Props
import brs.util.convert.toUnsignedString
import kotlinx.coroutines.sync.Mutex
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.util.concurrent.locks.StampedLock

class DownloadCacheImpl(private val dp: DependencyProvider) { // TODO interface
    private val blockCacheMB = dp.propertyService.get(Props.BRS_BLOCK_CACHE_MB)
    private val forkCache = mutableListOf<Block>()

    private val stampedLock = StampedLock() // We use the same lock for all of the variables.

    // All of these should only be accessed using the stampedLock
    private val blockCache = mutableMapOf<Long, Block>()
    private val reverseCache = mutableMapOf<Long, Long>()
    private val unverified = mutableListOf<Long>()
    private var blockCacheSizeInternal = 0
    private var lastBlockId: Long? = null
    private var lastHeight = -1
    private var highestCumulativeDifficulty = BigInteger.ZERO
    private var locked = false

    val mutex = Mutex()

    private val chainHeight: Int
        get() = stampedLock.read {
            val height = lastHeight
            return if (height > -1) height else dp.blockchain.height
        }

    val isFull: Boolean
        get() = stampedLock.read {
            blockCacheSizeInternal > blockCacheMB * 1024 * 1024
        }

    val cumulativeDifficulty: BigInteger
        get() {
            if (stampedLock.read { lastBlockId } != null) return stampedLock.read { highestCumulativeDifficulty }
            setLastVars()
            return stampedLock.read { highestCumulativeDifficulty }
        }

    val firstUnverifiedBlock: Block?
        get() = stampedLock.writeAndRead {
            if (unverified.isEmpty()) return@writeAndRead null
            val blockId = unverified[0]
            val block = blockCache[blockId]
            unverified.remove(blockId)
            block
        }

    val unverifiedSize
        get() = stampedLock.read { unverified.size }

    val blockCacheSize
        get() = stampedLock.read { blockCacheSizeInternal }

    val forkList: List<Block>
        get() = forkCache

    val lastBlock: Block?
        get() = stampedLock.read { blockCache[lastBlockId ?: return@read null] } ?: dp.blockchain.lastBlock

    fun lockCache() {
        stampedLock.write {
            locked = true
        }
        setLastVars()
    }

    fun unlockCache() {
        val isLocked = stampedLock.read { locked }
        if (isLocked) stampedLock.write { locked = false }
    }

    fun getUnverifiedBlockIdFromPos(pos: Int) = stampedLock.read { unverified[pos] }

    fun removeUnverified(blockId: Long) = stampedLock.write {
        unverified.remove(blockId)
    }

    fun removeUnverifiedBatch(blocks: Collection<Block>) = stampedLock.write {
        for (block in blocks) {
            unverified.remove(block.id)
        }
    }

    fun resetCache() {
        stampedLock.write {
            blockCache.clear()
            reverseCache.clear()
            unverified.clear()
            blockCacheSizeInternal = 0
            locked = true
        }
        setLastVars()
    }

    fun getBlock(blockId: Long): Block? {
        if (forkCache.isNotEmpty()) {
            for (block in forkCache) {
                if (block.id == blockId) {
                    return block
                }
            }
        }
        return stampedLock.read { blockCache[blockId] } ?: if (dp.blockchain.hasBlock(blockId)) dp.blockchain.getBlock(blockId) else null
    }

    fun getNextBlock(prevBlockId: Long) = stampedLock.read { blockCache[reverseCache[prevBlockId]] }

    fun hasBlock(blockId: Long) = stampedLock.read { blockCache.containsKey(blockId) } || dp.blockchain.hasBlock(blockId)

    fun canBeFork(oldBlockId: Long) = stampedLock.read {
        val curHeight = chainHeight
        var block = blockCache[oldBlockId]
        if (block == null && dp.blockchain.hasBlock(oldBlockId)) {
            block = dp.blockchain.getBlock(oldBlockId)
        }
        block != null && curHeight - block.height <= dp.propertyService.get(Props.DB_MAX_ROLLBACK)
    }

    fun addBlock(block: Block) = stampedLock.writeAndRead {
        if (locked) false else {
            blockCache[block.id] = block
            reverseCache[block.previousBlockId] = block.id
            unverified.add(block.id)
            blockCacheSizeInternal += block.byteLength
            lastBlockId = block.id
            lastHeight = block.height
            highestCumulativeDifficulty = block.cumulativeDifficulty
            true
        }
    }

    fun addForkBlock(block: Block) {
        forkCache.add(block)
    }

    fun resetForkBlocks() {
        forkCache.clear()
    }

    fun removeBlock(block: Block) = stampedLock.write {
        if (blockCache.containsKey(block.id)) { // make sure there is something to remove
            unverified.remove(block.id)
            reverseCache.remove(block.previousBlockId)
            blockCache.remove(block.id)
            blockCacheSizeInternal -= block.byteLength
        }
    }

    fun getPoCVersion(blockId: Long): Int {
        val block = getBlock(blockId)
        return if (block == null || !dp.fluxCapacitor.getValue(FluxValues.POC2, block.height)) 1 else 2
    }

    fun getLastBlockId() = stampedLock.read { lastBlockId } ?: dp.blockchain.lastBlock.id

    fun size() = stampedLock.read { blockCache.size }

    private fun printLastVars() {
        logger.debug("Cache LastId: {}", lastBlockId?.toUnsignedString())
        logger.debug("Cache lastHeight: {}", lastHeight)
    }

    private fun setLastVars() = stampedLock.write {
        if (blockCache.isNotEmpty()) {
            lastBlockId = blockCache[blockCache.keys.toTypedArray()[blockCache.keys.size - 1]]?.id
            lastHeight = blockCache[lastBlockId ?: Genesis.GENESIS_BLOCK_ID ]?.height ?: 0
            highestCumulativeDifficulty = blockCache[lastBlockId ?: Genesis.GENESIS_BLOCK_ID]!!.cumulativeDifficulty
            logger.debug("Cache set to CacheData")
            printLastVars() // TODO remove? or compact?
        } else {
            lastBlockId = dp.blockchain.lastBlock.id
            lastHeight = dp.blockchain.height
            highestCumulativeDifficulty = dp.blockchain.lastBlock.cumulativeDifficulty
            logger.debug("Cache set to ChainData")
            printLastVars() // TODO remove? or compact?
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DownloadCacheImpl::class.java)
    }
}
