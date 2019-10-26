package brs.services

import brs.entity.Account
import brs.entity.Block
import brs.entity.Transaction

interface BlockchainService {
    /**
     * TODO
     */
    var lastBlock: Block

    /**
     * TODO
     */
    val height: Int

    /**
     * TODO
     */
    fun getTransactionCount(): Int

    /**
     * TODO
     */
    fun getAllTransactions(): Collection<Transaction>

    /**
     * TODO
     */
    fun getLastBlock(timestamp: Int): Block?

    /**
     * TODO
     */
    fun getBlock(blockId: Long): Block?

    /**
     * TODO
     */
    fun getBlockAtHeight(height: Int): Block?

    /**
     * TODO
     */
    fun hasBlock(blockId: Long): Boolean

    /**
     * TODO
     */
    fun getBlocks(from: Int, to: Int): Collection<Block>

    /**
     * TODO
     */
    fun getBlocks(account: Account, timestamp: Int): Collection<Block>

    /**
     * TODO
     */
    fun getBlocks(account: Account, timestamp: Int, from: Int, to: Int): Collection<Block>

    /**
     * TODO
     */
    fun getBlockIdsAfter(blockId: Long, limit: Int): Collection<Long>

    /**
     * TODO
     */
    fun getBlocksAfter(blockId: Long, limit: Int): Collection<Block>

    /**
     * TODO
     */
    fun getBlockIdAtHeight(height: Int): Long

    /**
     * TODO
     */
    fun getTransaction(transactionId: Long): Transaction?

    /**
     * TODO
     */
    fun getTransactionByFullHash(fullHash: ByteArray): Transaction?

    /**
     * TODO
     */
    fun hasTransaction(transactionId: Long): Boolean

    /**
     * TODO
     */
    fun hasTransactionByFullHash(fullHash: ByteArray): Boolean

    /**
     * TODO
     */
    fun getTransactions(account: Account, type: Byte, subtype: Byte, blockTimestamp: Int, includeIndirectIncoming: Boolean): Collection<Transaction>

    /**
     * TODO
     */
    fun getTransactions(account: Account, numberOfConfirmations: Int, type: Byte, subtype: Byte, blockTimestamp: Int, from: Int, to: Int, includeIndirectIncoming: Boolean): Collection<Transaction>

    /**
     * TODO
     */
    fun setLastBlock(previousBlock: Block, block: Block)
}
