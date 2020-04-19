package brs.services.impl

import brs.entity.Account
import brs.entity.Block
import brs.entity.DependencyProvider
import brs.entity.Transaction
import brs.services.BlockchainService
import brs.util.delegates.AtomicLateinit
import brs.util.sync.read
import brs.util.sync.write
import java.util.concurrent.locks.StampedLock

class BlockchainServiceImpl internal constructor(private val dp: DependencyProvider) : BlockchainService {
    override var lastBlock by AtomicLateinit<Block>()
    private val lastBlockLock = StampedLock()

    override val height get() = lastBlock.height

    override fun getTransactionCount() = dp.db.blockchainStore.getTransactionCount()

    override fun getAllTransactions() = dp.db.blockchainStore.getAllTransactions()

    override fun setLastBlock(previousBlock: Block, block: Block) = lastBlockLock.write {
        if (lastBlock == previousBlock) {
            lastBlock = block
        }
    }

    override fun getLastBlock(timestamp: Int): Block? {
        val block = lastBlockLock.read { lastBlock }
        return if (timestamp >= block.timestamp) block else dp.db.blockDb.findLastBlock(timestamp)
    }

    override fun getBlock(blockId: Long): Block? {
        val block = lastBlockLock.read { lastBlock }
        return if (block.id == blockId) block else dp.db.blockDb.findBlock(blockId)
    }

    override fun hasBlock(blockId: Long): Boolean {
        return lastBlockLock.read { lastBlock.id } == blockId || dp.db.blockDb.hasBlock(blockId)
    }

    override fun getBlocks(from: Int, to: Int): Collection<Block> {
        return dp.db.blockchainStore.getBlocks(from, to)
    }

    override fun getBlocks(account: Account, timestamp: Int): Collection<Block> {
        return getBlocks(account, timestamp, 0, -1)
    }

    override fun getBlocks(account: Account, timestamp: Int, from: Int, to: Int): Collection<Block> {
        return dp.db.blockchainStore.getBlocks(account, timestamp, from, to)
    }

    override fun getBlockIdsAfter(blockId: Long, limit: Int): Collection<Long> {
        return dp.db.blockchainStore.getBlockIdsAfter(blockId, limit)
    }

    override fun getBlocksAfter(blockId: Long, limit: Int): Collection<Block> {
        return dp.db.blockchainStore.getBlocksAfter(blockId, limit)
    }

    override fun getBlockIdAtHeight(height: Int): Long {
        val block = lastBlockLock.read { lastBlock }
        require(height <= block.height) { "Invalid height " + height + ", current blockchain is at " + block.height }
        return if (height == block.height) block.id else dp.db.blockDb.findBlockIdAtHeight(height)
    }

    override fun getBlockAtHeight(height: Int): Block? {
        val block = lastBlockLock.read { lastBlock }
        require(height <= block.height) { "Invalid height " + height + ", current blockchain is at " + block.height }
        return if (height == block.height) block else dp.db.blockDb.findBlockAtHeight(height)
    }

    override fun getTransaction(transactionId: Long): Transaction? {
        return dp.db.transactionDb.findTransaction(transactionId)
    }

    override fun getTransactionByFullHash(fullHash: ByteArray): Transaction? {
        return dp.db.transactionDb.findTransactionByFullHash(fullHash)
    }

    override fun hasTransaction(transactionId: Long): Boolean {
        return dp.db.transactionDb.hasTransaction(transactionId)
    }

    override fun hasTransactionByFullHash(fullHash: ByteArray): Boolean {
        return dp.db.transactionDb.hasTransactionByFullHash(fullHash)
    }

    override fun getTransactions(
        account: Account,
        type: Byte,
        subtype: Byte,
        blockTimestamp: Int,
        includeIndirectIncoming: Boolean
    ): Collection<Transaction> {
        return getTransactions(account, 0, type, subtype, blockTimestamp, 0, -1, includeIndirectIncoming)
    }

    override fun getTransactions(
        account: Account,
        numberOfConfirmations: Int,
        type: Byte,
        subtype: Byte,
        blockTimestamp: Int,
        from: Int,
        to: Int,
        includeIndirectIncoming: Boolean
    ): Collection<Transaction> {
        return dp.db.blockchainStore.getTransactions(
            account,
            numberOfConfirmations,
            type,
            subtype,
            blockTimestamp,
            from,
            to,
            includeIndirectIncoming
        )
    }
}
