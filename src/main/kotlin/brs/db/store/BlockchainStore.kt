package brs.db.store

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

    fun getTransactionCount(): Int

    fun getAllTransactions(): Collection<Transaction>

    fun getBlocks(from: Int, to: Int): Collection<Block>

    fun getBlocks(account: Account, timestamp: Int, from: Int, to: Int): Collection<Block>

    fun getBlocks(blockRecords: Result<BlockRecord>): Collection<Block>

    fun getBlockIdsAfter(blockId: Long, limit: Int): Collection<Long>

    fun getBlocksAfter(blockId: Long, limit: Int): Collection<Block>

    fun getTransactions(account: Account, numberOfConfirmations: Int, type: Byte, subtype: Byte,
                        blockTimestamp: Int, from: Int, to: Int, includeIndirectIncoming: Boolean): Collection<Transaction>

    fun getTransactions(ctx: DSLContext, rs: Result<TransactionRecord>): Collection<Transaction>

    fun addBlock(block: Block)

    fun getLatestBlocks(amountBlocks: Int): Collection<Block>
}
