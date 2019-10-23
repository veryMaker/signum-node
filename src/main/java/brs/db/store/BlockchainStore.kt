package brs.db.store

import brs.Account
import brs.Block
import brs.Transaction
import brs.schema.tables.records.BlockRecord
import brs.schema.tables.records.TransactionRecord
import org.jooq.DSLContext
import org.jooq.Result

/**
 * Store for both BlockchainImpl and BlockchainProcessorImpl
 */

interface BlockchainStore {

    suspend fun getTransactionCount(): Int

    suspend fun getAllTransactions(): Collection<Transaction>

    suspend fun getBlocks(from: Int, to: Int): Collection<Block>

    suspend fun getBlocks(account: Account, timestamp: Int, from: Int, to: Int): Collection<Block>

    fun getBlocks(blockRecords: Result<BlockRecord>): Collection<Block>

    suspend fun getBlockIdsAfter(blockId: Long, limit: Int): Collection<Long>

    suspend fun getBlocksAfter(blockId: Long, limit: Int): Collection<Block>

    suspend fun getTransactions(account: Account, numberOfConfirmations: Int, type: Byte, subtype: Byte,
                                blockTimestamp: Int, from: Int, to: Int, includeIndirectIncoming: Boolean): Collection<Transaction>

    fun getTransactions(ctx: DSLContext, rs: Result<TransactionRecord>): Collection<Transaction>

    suspend fun addBlock(block: Block)

    suspend fun getLatestBlocks(amountBlocks: Int): Collection<Block>
}
