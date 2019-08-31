package brs

import brs.db.BlockDb
import brs.db.TransactionDb
import brs.db.store.BlockchainStore
import brs.util.StampedLockUtils
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.StampedLock
import java.util.function.Supplier

class BlockchainImpl internal constructor(private val dp: DependencyProvider) : Blockchain {

    private val bcsl: StampedLock

    private val lastBlock = AtomicReference<Block>()

    override val height: Int
        get() {
            val last = getLastBlock()
            return last?.height ?: 0
        }

    override val transactionCount: Int
        get() = dp.blockchainStore.transactionCount

    override val allTransactions: Collection<Transaction>
        get() = dp.blockchainStore.allTransactions

    init {
        this.bcsl = StampedLock()
    }

    private fun <T> bcslRead(supplier: Supplier<T>): T? {
        return StampedLockUtils.stampedLockRead(bcsl, supplier)
    }

    override fun getLastBlock(): Block {
        return bcslRead(Supplier { lastBlock.get() })
    }

    override fun setLastBlock(block: Block) {
        val stamp = bcsl.writeLock()
        try {
            lastBlock.set(block)
        } finally {
            bcsl.unlockWrite(stamp)
        }
    }

    override fun setLastBlock(previousBlock: Block, block: Block) {
        val stamp = bcsl.writeLock()
        try {
            check(lastBlock.compareAndSet(previousBlock, block)) { "Last block is no longer previous block" }
        } finally {
            bcsl.unlockWrite(stamp)
        }
    }

    override fun getLastBlock(timestamp: Int): Block? {
        val block = getLastBlock()
        return if (timestamp >= block.timestamp) {
            block
        } else dp.dbs.blockDb.findLastBlock(timestamp)
    }

    override fun getBlock(blockId: Long): Block? {
        val block = getLastBlock()
        return if (block.id == blockId) {
            block
        } else dp.dbs.blockDb.findBlock(blockId)
    }

    override fun hasBlock(blockId: Long): Boolean {
        return getLastBlock().id == blockId || dp.dbs.blockDb.hasBlock(blockId)
    }

    override fun getBlocks(from: Int, to: Int): Collection<Block> {
        return dp.blockchainStore.getBlocks(from, to)
    }

    override fun getBlocks(account: Account, timestamp: Int): Collection<Block> {
        return getBlocks(account, timestamp, 0, -1)
    }

    override fun getBlocks(account: Account, timestamp: Int, from: Int, to: Int): Collection<Block> {
        return dp.blockchainStore.getBlocks(account, timestamp, from, to)
    }

    override fun getBlockIdsAfter(blockId: Long, limit: Int): Collection<Long> {
        return dp.blockchainStore.getBlockIdsAfter(blockId, limit)
    }

    override fun getBlocksAfter(blockId: Long, limit: Int): Collection<Block> {
        return dp.blockchainStore.getBlocksAfter(blockId, limit)
    }

    override fun getBlockIdAtHeight(height: Int): Long {
        val block = getLastBlock()
        require(height <= block.height) { "Invalid height " + height + ", current blockchain is at " + block.height }
        return if (height == block.height) {
            block.id
        } else dp.dbs.blockDb.findBlockIdAtHeight(height)
    }

    override fun getBlockAtHeight(height: Int): Block? {
        val block = getLastBlock()
        require(height <= block.height) { "Invalid height " + height + ", current blockchain is at " + block.height }
        return if (height == block.height) {
            block
        } else dp.dbs.blockDb.findBlockAtHeight(height)
    }

    override fun getTransaction(transactionId: Long): Transaction? {
        return dp.dbs.transactionDb.findTransaction(transactionId)
    }

    override fun getTransactionByFullHash(fullHash: String): Transaction? {
        return dp.dbs.transactionDb.findTransactionByFullHash(fullHash)
    }

    override fun hasTransaction(transactionId: Long): Boolean {
        return dp.dbs.transactionDb.hasTransaction(transactionId)
    }

    override fun hasTransactionByFullHash(fullHash: String): Boolean {
        return dp.dbs.transactionDb.hasTransactionByFullHash(fullHash)
    }

    override fun getTransactions(account: Account, type: Byte, subtype: Byte, blockTimestamp: Int, includeIndirectIncoming: Boolean): Collection<Transaction> {
        return getTransactions(account, 0, type, subtype, blockTimestamp, 0, -1, includeIndirectIncoming)
    }

    override fun getTransactions(account: Account, numberOfConfirmations: Int, type: Byte, subtype: Byte,
                                 blockTimestamp: Int, from: Int, to: Int, includeIndirectIncoming: Boolean): Collection<Transaction> {
        return dp.blockchainStore.getTransactions(account, numberOfConfirmations, type, subtype, blockTimestamp, from, to, includeIndirectIncoming)
    }
}
