package brs.util

import brs.Block
import brs.Blockchain
import brs.Constants
import brs.Genesis
import brs.fluxcapacitor.FluxCapacitor
import brs.fluxcapacitor.FluxValues
import brs.props.PropertyService
import brs.props.Props
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.util.*
import java.util.concurrent.locks.StampedLock
import java.util.function.Supplier

class DownloadCacheImpl(propertyService: PropertyService, private val fluxCapacitor: FluxCapacitor, private val blockchain: Blockchain) {
    private val blockCacheMB = propertyService.get(Props.BRS_BLOCK_CACHE_MB)

    private val blockCache = LinkedHashMap<Long, Block>()
    private val forkCache = ArrayList<Block>()
    private val reverseCache = LinkedHashMap<Long, Long>()
    private val unverified = LinkedList<Long>()

    private val logger = LoggerFactory.getLogger(DownloadCacheImpl::class.java)

    private var blockCacheSize = 0

    private var lastBlockId: Long? = null
    private var lastHeight = -1
    private var highestCumulativeDifficulty = BigInteger.ZERO

    private val dcsl = StampedLock()

    private var lockedCache = false

    private val chainHeight: Int // TODO replace dcsl usages
        get() {
            var stamp = dcsl.tryOptimisticRead()
            var retVal = lastHeight
            if (!dcsl.validate(stamp)) {
                stamp = dcsl.readLock()
                try {
                    retVal = lastHeight
                } finally {
                    dcsl.unlockRead(stamp)
                }
            }
            return if (retVal > -1) {
                retVal
            } else blockchain.height
        }
    private val lockState: Boolean
        get() {
            var stamp = dcsl.tryOptimisticRead()
            var retVal = lockedCache
            if (!dcsl.validate(stamp)) {
                stamp = dcsl.readLock()
                try {
                    retVal = lockedCache
                } finally {
                    dcsl.unlockRead(stamp)
                }
            }
            return retVal
        }


    val isFull: Boolean
        get() {
            var stamp = dcsl.tryOptimisticRead()
            var retVal = blockCacheSize
            if (!dcsl.validate(stamp)) {

                stamp = dcsl.readLock()
                try {
                    retVal = blockCacheSize
                } finally {
                    dcsl.unlockRead(stamp)
                }
            }
            return retVal > blockCacheMB * 1024 * 1024
        }

    val unverifiedSize: Int
        get() {
            var stamp = dcsl.tryOptimisticRead()
            var retVal = unverified.size
            if (!dcsl.validate(stamp)) {
                stamp = dcsl.readLock()
                try {
                    retVal = unverified.size
                } finally {
                    dcsl.unlockRead(stamp)
                }
            }
            return retVal
        }

    val cumulativeDifficulty: BigInteger
        get() {
            var stamp = dcsl.tryOptimisticRead()
            var lbID = lastBlockId
            var retVal = highestCumulativeDifficulty


            if (!dcsl.validate(stamp)) {

                stamp = dcsl.readLock()
                try {
                    lbID = lastBlockId
                    retVal = highestCumulativeDifficulty
                } finally {
                    dcsl.unlockRead(stamp)
                }
            }
            if (lbID != null) {
                return retVal
            }
            setLastVars()
            stamp = dcsl.tryOptimisticRead()
            retVal = highestCumulativeDifficulty
            if (!dcsl.validate(stamp)) {
                stamp = dcsl.readLock()
                try {
                    retVal = highestCumulativeDifficulty
                } finally {
                    dcsl.unlockRead(stamp)
                }
            }
            return retVal
        }

    val firstUnverifiedBlock: Block?
        get() {
            val stamp = dcsl.writeLock()
            try {
                val blockId = unverified[0]
                val block = blockCache[blockId]
                unverified.remove(blockId)
                return block
            } finally {
                dcsl.unlockWrite(stamp)
            }
        }

    val forkList: List<Block>
        get() = forkCache

    private val lastCacheId: Long?
        get() {
            var stamp = dcsl.tryOptimisticRead()
            var lId = lastBlockId
            if (!dcsl.validate(stamp)) {

                stamp = dcsl.readLock()
                try {
                    lId = lastBlockId
                } finally {
                    dcsl.unlockRead(stamp)
                }
            }
            return lId
        }

    val lastBlock: Block?
        get() {
            val iLd = lastCacheId
            return if (iLd != null && blockCache.containsKey(iLd)) {
                dcslRead(Supplier { blockCache[iLd] })
            } else blockchain.lastBlock
        }


    fun lockCache() {
        val stamp = dcsl.writeLock()
        try {
            lockedCache = true
        } finally {
            dcsl.unlockWrite(stamp)
        }
        setLastVars()
    }

    fun unlockCache() {
        var stamp = dcsl.tryOptimisticRead()
        var retVal = lockedCache
        if (!dcsl.validate(stamp)) {
            stamp = dcsl.readLock()
            try {
                retVal = lockedCache
            } finally {
                dcsl.unlockRead(stamp)
            }
        }

        if (retVal) {
            stamp = dcsl.writeLock()
            try {
                lockedCache = false
            } finally {
                dcsl.unlockWrite(stamp)
            }
        }
    }

    fun getBlockCacheSize(): Int {
        var stamp = dcsl.tryOptimisticRead()
        var retVal = blockCacheSize
        if (!dcsl.validate(stamp)) {

            stamp = dcsl.readLock()
            try {
                retVal = blockCacheSize
            } finally {
                dcsl.unlockRead(stamp)
            }
        }
        return retVal
    }

    fun getUnverifiedBlockIdFromPos(pos: Int): Long {
        var stamp = dcsl.tryOptimisticRead()
        var reVal = unverified[pos]

        if (!dcsl.validate(stamp)) {

            stamp = dcsl.readLock()
            try {
                reVal = unverified[pos]
            } finally {
                dcsl.unlockRead(stamp)
            }
        }
        return reVal
    }

    fun removeUnverified(blockId: Long) {
        val stamp = dcsl.writeLock()
        try {
            unverified.remove(blockId)
        } finally {
            dcsl.unlockWrite(stamp)
        }
    }

    fun removeUnverifiedBatch(blocks: Collection<Block>) {
        val stamp = dcsl.writeLock()
        try {
            for (block in blocks) {
                unverified.remove(block.id)
            }
        } finally {
            dcsl.unlockWrite(stamp)
        }
    }

    fun resetCache() {
        val stamp = dcsl.writeLock()
        try {
            blockCache.clear()
            reverseCache.clear()
            unverified.clear()
            blockCacheSize = 0
            lockedCache = true
        } finally {
            dcsl.unlockWrite(stamp)
        }
        setLastVars()
    }

    fun getBlock(blockId: Long): Block? {
        //search the forkCache if we have a forkList
        if (!forkCache.isEmpty()) {
            for (block in forkCache) {
                if (block.id == blockId) {
                    return block
                }
            }
        }
        var stamp = dcsl.tryOptimisticRead()
        var retVal = getBlockInt(blockId)
        if (!dcsl.validate(stamp)) {
            stamp = dcsl.readLock()
            try {
                retVal = getBlockInt(blockId)
            } finally {
                dcsl.unlockRead(stamp)
            }
        }
        if (retVal != null) {
            return retVal
        }
        return if (blockchain.hasBlock(blockId)) {
            blockchain.getBlock(blockId)
        } else null
    }

    private fun getBlockInt(blockId: Long): Block? {
        return if (blockCache.containsKey(blockId)) {
            blockCache[blockId]
        } else null
    }

    fun getNextBlock(prevBlockId: Long): Block? {
        var stamp = dcsl.tryOptimisticRead()
        var retVal = getNextBlockInt(prevBlockId)
        if (!dcsl.validate(stamp)) {

            stamp = dcsl.readLock()
            try {
                retVal = getNextBlockInt(prevBlockId)
            } finally {
                dcsl.unlockRead(stamp)
            }
        }
        return retVal
    }

    private fun getNextBlockInt(prevBlockId: Long): Block? {
        return if (reverseCache.containsKey(prevBlockId)) {
            blockCache[reverseCache[prevBlockId]]
        } else null
    }

    fun hasBlock(blockId: Long): Boolean {
        var stamp = dcsl.tryOptimisticRead()
        var retVal = blockCache.containsKey(blockId)
        if (!dcsl.validate(stamp)) {

            stamp = dcsl.readLock()
            try {
                retVal = blockCache.containsKey(blockId)
            } finally {
                dcsl.unlockRead(stamp)
            }
        }
        return if (retVal) {
            true
        } else blockchain.hasBlock(blockId)
    }

    fun canBeFork(oldBlockId: Long): Boolean {
        val curHeight = chainHeight
        var block: Block? = dcslRead(Supplier { getBlockInt(oldBlockId) })
        if (block == null && blockchain.hasBlock(oldBlockId)) {
            block = blockchain.getBlock(oldBlockId)
        }
        return if (block == null) {
            false
        } else curHeight - block.height <= Constants.MAX_ROLLBACK
    }

    fun addBlock(block: Block): Boolean {
        if (!lockState) {
            val stamp = dcsl.writeLock()
            try {
                blockCache[block.id] = block
                reverseCache[block.previousBlockId] = block.id
                unverified.add(block.id)
                blockCacheSize += block.byteLength
                lastBlockId = block.id
                lastHeight = block.height
                highestCumulativeDifficulty = block.cumulativeDifficulty
                return true
            } finally {
                dcsl.unlockWrite(stamp)
            }
        }
        return false
    }

    fun addForkBlock(block: Block) {
        forkCache.add(block)
    }

    fun resetForkBlocks() {
        forkCache.clear()
    }

    fun removeBlock(block: Block): Boolean {
        var stamp = dcsl.tryOptimisticRead()
        var chkVal = blockCache.containsKey(block.id)
        val lastId = lastBlockId

        if (!dcsl.validate(stamp)) {
            stamp = dcsl.readLock()
            try {
                chkVal = blockCache.containsKey(block.id)
            } finally {
                dcsl.unlockRead(stamp)
            }
        }

        if (chkVal) { // make sure there is something to remove
            stamp = dcsl.writeLock()
            try {
                unverified.remove(block.id)
                reverseCache.remove(block.previousBlockId)
                blockCache.remove(block.id)
                blockCacheSize -= block.byteLength
            } finally {
                dcsl.unlockWrite(stamp)
            }
            if (block.id == lastId) {
                setLastVars()
            }
            return true
        }
        return false
    }

    fun getPoCVersion(blockId: Long): Int {
        val blockImpl = getBlock(blockId)
        return if (blockImpl == null || !fluxCapacitor.getValue(FluxValues.POC2, blockImpl.height)) 1 else 2
    }

    fun getLastBlockId(): Long {
        val lId = lastCacheId
        return lId ?: blockchain.lastBlock.id
    }

    private fun <T> dcslRead(supplier: Supplier<T?>): T? {
        return StampedLockUtils.stampedLockRead(dcsl, supplier)
    }

    fun size(): Int {
        return dcslRead<Int>(Supplier { blockCache.size }) ?: 0
    }

    fun printDebug() {
        logger.info("BlockCache size: {}", blockCache.size)
        logger.info("Unverified size: {}", unverified.size)
        logger.info("Verified size: {}", blockCache.size - unverified.size)

    }

    private fun printLastVars() {
        logger.debug("Cache LastId: {}", lastBlockId)
        logger.debug("Cache lastHeight: {}", lastHeight)
    }


    private fun setLastVars() {
        val stamp = dcsl.writeLock()
        try {
            if (!blockCache.isEmpty()) {
                lastBlockId = blockCache[blockCache.keys.toTypedArray()[blockCache.keys.size - 1]]?.id
                lastHeight = blockCache[lastBlockId ?: Genesis.GENESIS_BLOCK_ID ]?.height ?: 0
                highestCumulativeDifficulty = blockCache[lastBlockId ?: Genesis.GENESIS_BLOCK_ID]?.cumulativeDifficulty
                logger.debug("Cache set to CacheData")
                printLastVars()
            } else {
                lastBlockId = blockchain.lastBlock.id
                lastHeight = blockchain.height
                highestCumulativeDifficulty = blockchain.lastBlock.cumulativeDifficulty
                logger.debug("Cache set to ChainData")
                printLastVars()
            }
        } finally {
            dcsl.unlockWrite(stamp)
        }
    }
}
