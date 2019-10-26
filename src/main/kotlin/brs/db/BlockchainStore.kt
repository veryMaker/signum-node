package brs.db

import brs.entity.Account
import brs.entity.Block
import brs.entity.Transaction
import brs.schema.tables.records.BlockRecord
import brs.schema.tables.records.TransactionRecord
import org.jooq.DSLContext
import org.jooq.Result

/**
 * Store for both BlockchainImpl and BlockchainProcessorImpl
 */

interface BlockchainStore {
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
    fun getBlocks(from: Int, to: Int): Collection<Block>

    /**
     * TODO
     */
    fun getBlocks(account: Account, timestamp: Int, from: Int, to: Int): Collection<Block>

    /**
     * TODO
     */
    fun getBlocks(blockRecords: Result<BlockRecord>): Collection<Block>

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
    fun getTransactions(account: Account, numberOfConfirmations: Int, type: Byte, subtype: Byte, blockTimestamp: Int, from: Int, to: Int, includeIndirectIncoming: Boolean): Collection<Transaction>

    /**
     * TODO
     */
    fun getTransactions(ctx: DSLContext, rs: Result<TransactionRecord>): Collection<Transaction>

    /**
     * TODO
     */
    fun addBlock(block: Block)

    /**
     * TODO
     */
    fun getLatestBlocks(amountBlocks: Int): Collection<Block>
}
