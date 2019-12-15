package brs.services.impl

import brs.entity.Block
import brs.entity.DependencyProvider
import brs.objects.FluxValues
import brs.objects.Genesis
import brs.objects.Props
import brs.services.DownloadCacheService
import brs.util.convert.toUnsignedString
import brs.util.logging.safeDebug
import brs.util.sync.Mutex
import brs.util.sync.read
import brs.util.sync.write
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.util.concurrent.locks.StampedLock

class DownloadCacheServiceImpl(private val dp: DependencyProvider) : DownloadCacheService {
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

    override val mutex = Mutex()

    private val chainHeight: Int
        get() = stampedLock.read {
            val height = lastHeight
            return if (height > -1) height else dp.blockchainService.height
        }

    override val isFull: Boolean
        get() = stampedLock.read {
            blockCacheSizeInternal > blockCacheMB * 1024 * 1024
        }

    override val cumulativeDifficulty: BigInteger
        get() {
            if (stampedLock.read { lastBlockId } != null) return stampedLock.read { highestCumulativeDifficulty }
            setLastVars()
            return stampedLock.read { highestCumulativeDifficulty }
        }

    override val firstUnverifiedBlock: Block?
        get() = stampedLock.write {
            if (unverified.isEmpty()) return@write null
            val blockId = unverified[0]
            val block = blockCache[blockId]
            unverified.remove(blockId)
            block
        }

    override val unverifiedSize
        get() = stampedLock.read { unverified.size }

    override val blockCacheSize
        get() = stampedLock.read { blockCacheSizeInternal }

    override val forkList: List<Block>
        get() = forkCache

    override val lastBlock: Block
        get() = stampedLock.read { blockCache[lastBlockId] } ?: dp.blockchainService.lastBlock

    override fun lockCache() {
        stampedLock.write {
            locked = true
        }
        setLastVars()
    }

    override fun unlockCache() {
        val isLocked = stampedLock.read { locked }
        if (isLocked) stampedLock.write { locked = false }
    }

    override fun getUnverifiedBlockIdFromPos(pos: Int) = stampedLock.read { unverified[pos] }

    override fun removeUnverified(blockId: Long) = stampedLock.write<Unit> {
        unverified.remove(blockId)
    }

    override fun removeUnverifiedBatch(blocks: Collection<Block>) = stampedLock.write {
        for (block in blocks) {
            unverified.remove(block.id)
        }
    }

    override fun resetCache() {
        stampedLock.write {
            blockCache.clear()
            reverseCache.clear()
            unverified.clear()
            blockCacheSizeInternal = 0
            locked = true
        }
        setLastVars()
    }

    override fun getBlock(blockId: Long): Block? {
        if (forkCache.isNotEmpty()) {
            for (block in forkCache) {
                if (block.id == blockId) return block
            }
        }
        return stampedLock.read { blockCache[blockId] } ?: if (dp.blockchainService.hasBlock(blockId)) dp.blockchainService.getBlock(blockId) else null
    }

    override fun getNextBlock(prevBlockId: Long) = stampedLock.read { blockCache[reverseCache[prevBlockId]] }

    override fun hasBlock(blockId: Long) =
        stampedLock.read { blockCache.containsKey(blockId) } || dp.blockchainService.hasBlock(blockId)

    override fun canBeFork(oldBlockId: Long) = stampedLock.read {
        val curHeight = chainHeight
        var block = blockCache[oldBlockId]
        if (block == null && dp.blockchainService.hasBlock(oldBlockId)) {
            block = dp.blockchainService.getBlock(oldBlockId)
        }
        block != null && curHeight - block.height <= dp.propertyService.get(Props.DB_MAX_ROLLBACK)
    }

    override fun addBlock(block: Block) = stampedLock.write {
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

    override fun addForkBlock(block: Block) {
        forkCache.add(block)
    }

    override fun resetForkBlocks() {
        forkCache.clear()
    }

    override fun removeBlock(block: Block) = stampedLock.write {
        if (blockCache.containsKey(block.id)) { // make sure there is something to remove
            unverified.remove(block.id)
            reverseCache.remove(block.previousBlockId)
            blockCache.remove(block.id)
            blockCacheSizeInternal -= block.byteLength
        }
    }

    override fun getPoCVersion(blockId: Long): Int {
        val block = getBlock(blockId)
        return if (block == null || !dp.fluxCapacitorService.getValue(FluxValues.POC2, block.height)) 1 else 2
    }

    override fun getLastBlockId() = stampedLock.read { lastBlockId } ?: dp.blockchainService.lastBlock.id

    override fun size() = stampedLock.read { blockCache.size }

    private fun printLastVars() {
        logger.safeDebug { "Cache LastId: ${lastBlockId?.toUnsignedString()}" }
        logger.safeDebug { "Cache lastHeight: $lastHeight" }
    }

    private fun setLastVars() = stampedLock.write {
        if (blockCache.isNotEmpty()) {
            lastBlockId = blockCache[blockCache.keys.toTypedArray()[blockCache.keys.size - 1]]?.id
            lastHeight = blockCache[lastBlockId ?: Genesis.GENESIS_BLOCK_ID]?.height ?: 0
            highestCumulativeDifficulty = blockCache[lastBlockId ?: Genesis.GENESIS_BLOCK_ID]!!.cumulativeDifficulty
            logger.safeDebug { "Cache set to CacheData" }
            printLastVars()
        } else {
            lastBlockId = dp.blockchainService.lastBlock.id
            lastHeight = dp.blockchainService.height
            highestCumulativeDifficulty = dp.blockchainService.lastBlock.cumulativeDifficulty
            logger.safeDebug { "Cache set to ChainData" }
            printLastVars()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DownloadCacheServiceImpl::class.java)
    }
}
