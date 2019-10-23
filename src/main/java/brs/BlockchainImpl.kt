package brs

import brs.util.delegates.Atomic
import brs.util.read
import brs.util.write
import java.util.concurrent.locks.StampedLock

class BlockchainImpl internal constructor(private val dp: DependencyProvider) : Blockchain {
    override var lastBlock by Atomic<Block>() // TODO should this be null?
    private val lastBlockLock = StampedLock() // TODO

    override val height get() = lastBlock.height

    override suspend fun getTransactionCount() = dp.blockchainStore.getTransactionCount()

    override suspend fun getAllTransactions() = dp.blockchainStore.getAllTransactions()

    override fun setLastBlock(previousBlock: Block, block: Block) = lastBlockLock.write {
        if (lastBlock == previousBlock) {
            lastBlock = block
        }
    }

    override suspend fun getLastBlock(timestamp: Int): Block? {
        val block = lastBlockLock.read { lastBlock }
        return if (timestamp >= block.timestamp) block else dp.blockDb.findLastBlock(timestamp)
    }

    override suspend fun getBlock(blockId: Long): Block? {
        val block = lastBlockLock.read { lastBlock }
        return if (block.id == blockId) block else dp.blockDb.findBlock(blockId)
    }

    override suspend fun hasBlock(blockId: Long): Boolean {
        return lastBlockLock.read { lastBlock }.id == blockId || dp.blockDb.hasBlock(blockId)
    }

    override suspend fun getBlocks(from: Int, to: Int): Collection<Block> {
        return dp.blockchainStore.getBlocks(from, to)
    }

    override suspend fun getBlocks(account: Account, timestamp: Int): Collection<Block> {
        return getBlocks(account, timestamp, 0, -1)
    }

    override suspend fun getBlocks(account: Account, timestamp: Int, from: Int, to: Int): Collection<Block> {
        return dp.blockchainStore.getBlocks(account, timestamp, from, to)
    }

    override suspend fun getBlockIdsAfter(blockId: Long, limit: Int): Collection<Long> {
        return dp.blockchainStore.getBlockIdsAfter(blockId, limit)
    }

    override suspend fun getBlocksAfter(blockId: Long, limit: Int): Collection<Block> {
        return dp.blockchainStore.getBlocksAfter(blockId, limit)
    }

    override suspend fun getBlockIdAtHeight(height: Int): Long {
        val block = lastBlockLock.read { lastBlock }
        require(height <= block.height) { "Invalid height " + height + ", current blockchain is at " + block.height }
        return if (height == block.height) block.id else dp.blockDb.findBlockIdAtHeight(height)
    }

    override suspend fun getBlockAtHeight(height: Int): Block? {
        val block = lastBlockLock.read { lastBlock }
        require(height <= block.height) { "Invalid height " + height + ", current blockchain is at " + block.height }
        return if (height == block.height) block else dp.blockDb.findBlockAtHeight(height)
    }

    override suspend fun getTransaction(transactionId: Long): Transaction? {
        return dp.transactionDb.findTransaction(transactionId)
    }

    override suspend fun getTransactionByFullHash(fullHash: ByteArray): Transaction? {
        return dp.transactionDb.findTransactionByFullHash(fullHash)
    }

    override suspend fun hasTransaction(transactionId: Long): Boolean {
        return dp.transactionDb.hasTransaction(transactionId)
    }

    override suspend fun hasTransactionByFullHash(fullHash: ByteArray): Boolean {
        return dp.transactionDb.hasTransactionByFullHash(fullHash)
    }

    override suspend fun getTransactions(account: Account, type: Byte, subtype: Byte, blockTimestamp: Int, includeIndirectIncoming: Boolean): Collection<Transaction> {
        return getTransactions(account, 0, type, subtype, blockTimestamp, 0, -1, includeIndirectIncoming)
    }

    override suspend fun getTransactions(account: Account, numberOfConfirmations: Int, type: Byte, subtype: Byte, blockTimestamp: Int, from: Int, to: Int, includeIndirectIncoming: Boolean): Collection<Transaction> {
        return dp.blockchainStore.getTransactions(account, numberOfConfirmations, type, subtype, blockTimestamp, from, to, includeIndirectIncoming)
    }
}
