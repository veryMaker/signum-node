package brs

import brs.util.delegates.Atomic
import brs.util.read
import brs.util.write
import java.util.concurrent.locks.StampedLock

class BlockchainImpl internal constructor(private val dp: DependencyProvider) : Blockchain {
    override var lastBlock by Atomic<Block>() // TODO should this be null?
    private val lastBlockLock = StampedLock()

    override val height: Int
        get() = lastBlock.height

    override val transactionCount: Int
        get() = dp.blockchainStore.transactionCount

    override val allTransactions: Collection<Transaction>
        get() = dp.blockchainStore.allTransactions

    override fun setLastBlock(previousBlock: Block, block: Block) = lastBlockLock.write {
        if (lastBlock == previousBlock) {
            lastBlock = block
        }
    }

    override fun getLastBlock(timestamp: Int): Block? {
        val block = lastBlockLock.read { lastBlock }
        return if (timestamp >= block.timestamp) block else dp.dbs.blockDb.findLastBlock(timestamp)
    }

    override fun getBlock(blockId: Long): Block? {
        val block = lastBlockLock.read { lastBlock }
        return if (block.id == blockId) block else dp.dbs.blockDb.findBlock(blockId)
    }

    override fun hasBlock(blockId: Long): Boolean {
        return lastBlockLock.read { lastBlock }.id == blockId || dp.dbs.blockDb.hasBlock(blockId)
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
        val block = lastBlockLock.read { lastBlock }
        require(height <= block.height) { "Invalid height " + height + ", current blockchain is at " + block.height }
        return if (height == block.height) block.id else dp.dbs.blockDb.findBlockIdAtHeight(height)
    }

    override fun getBlockAtHeight(height: Int): Block? {
        val block = lastBlockLock.read { lastBlock }
        require(height <= block.height) { "Invalid height " + height + ", current blockchain is at " + block.height }
        return if (height == block.height) block else dp.dbs.blockDb.findBlockAtHeight(height)
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

    override fun getTransactions(account: Account, numberOfConfirmations: Int, type: Byte, subtype: Byte, blockTimestamp: Int, from: Int, to: Int, includeIndirectIncoming: Boolean): Collection<Transaction> {
        return dp.blockchainStore.getTransactions(account, numberOfConfirmations, type, subtype, blockTimestamp, from, to, includeIndirectIncoming)
    }
}
