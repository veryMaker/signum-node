package brs.db.sql

import brs.*
import brs.db.store.BlockchainStore
import brs.schema.Tables.BLOCK
import brs.schema.Tables.TRANSACTION
import brs.schema.tables.records.BlockRecord
import brs.schema.tables.records.TransactionRecord
import brs.util.db.fetchAndMap
import brs.util.db.inlineMap
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Result
import kotlin.math.max

class SqlBlockchainStore(private val dp: DependencyProvider) : BlockchainStore {
    override fun getTransactionCount() = dp.db.getUsingDslContext<Int> { ctx -> ctx.selectCount().from(TRANSACTION).fetchOne(0, Int::class.javaPrimitiveType) }

    override fun getAllTransactions() = dp.db.getUsingDslContext { ctx -> getTransactions(ctx, ctx.selectFrom(TRANSACTION).orderBy(TRANSACTION.DB_ID.asc()).fetch()) }

    override fun getBlocks(from: Int, to: Int): Collection<Block> {
        return dp.db.getUsingDslContext<Collection<Block>> { ctx ->
            val blockchainHeight = dp.blockchain.height
            getBlocks(ctx.selectFrom(BLOCK)
                    .where(BLOCK.HEIGHT.between(if (to > 0) blockchainHeight - to else 0).and(blockchainHeight - max(from, 0)))
                    .orderBy(BLOCK.HEIGHT.desc())
                    .fetch())
        }
    }

    override fun getBlocks(account: Account, timestamp: Int, from: Int, to: Int): Collection<Block> {
        return dp.db.getUsingDslContext<Collection<Block>> { ctx ->
            val query = ctx.selectFrom(BLOCK).where(BLOCK.GENERATOR_ID.eq(account.id))
            if (timestamp > 0) {
                query.and(BLOCK.TIMESTAMP.ge(timestamp))
            }
            getBlocks(query.orderBy(BLOCK.HEIGHT.desc()).fetch())
        }
    }

    override fun getBlocks(blockRecords: Result<BlockRecord>): Collection<Block> {
        return blockRecords.inlineMap { blockRecord ->
            try {
                return@inlineMap dp.blockDb.loadBlock(blockRecord)
            } catch (e: BurstException.ValidationException) {
                throw RuntimeException(e)
            }
        }
    }

    override fun getBlockIdsAfter(blockId: Long, limit: Int): Collection<Long> {
        require(limit <= 1440) { "Can't get more than 1440 blocks at a time" }

        return dp.db.getUsingDslContext<List<Long>> { ctx ->
            ctx.selectFrom(BLOCK).where(
                    BLOCK.HEIGHT.gt(ctx.select(BLOCK.HEIGHT).from(BLOCK).where(BLOCK.ID.eq(blockId)))
            ).orderBy(BLOCK.HEIGHT.asc()).limit(limit).fetch(BLOCK.ID, Long::class.java)
        }
    }

    override fun getBlocksAfter(blockId: Long, limit: Int): Collection<Block> {
        require(limit <= 1440) { "Can't get more than 1440 blocks at a time" }
        return dp.db.getUsingDslContext<List<Block>> { ctx ->
            ctx.selectFrom(BLOCK)
                    .where(BLOCK.HEIGHT.gt(ctx.select(BLOCK.HEIGHT).from(BLOCK).where(BLOCK.ID.eq(blockId))))
                    .orderBy(BLOCK.HEIGHT.asc())
                    .limit(limit)
                    .fetchAndMap { result ->
                        try {
                            return@fetchAndMap dp.blockDb.loadBlock(result)
                        } catch (e: BurstException.ValidationException) {
                            throw RuntimeException(e.toString(), e)
                        }
                    }
        }
    }

    override fun getTransactions(account: Account, numberOfConfirmations: Int, type: Byte, subtype: Byte, blockTimestamp: Int, from: Int, to: Int, includeIndirectIncoming: Boolean): Collection<Transaction> {
        val height = if (numberOfConfirmations > 0) dp.blockchain.height - numberOfConfirmations else Integer.MAX_VALUE
        require(height >= 0) { "Number of confirmations required " + numberOfConfirmations + " exceeds current blockchain height " + dp.blockchain.height }
        return dp.db.getUsingDslContext<Collection<Transaction>> { ctx ->
            val conditions = mutableListOf<Condition>()
            if (blockTimestamp > 0) {
                conditions.add(TRANSACTION.BLOCK_TIMESTAMP.ge(blockTimestamp))
            }
            if (type >= 0) {
                conditions.add(TRANSACTION.TYPE.eq(type))
                if (subtype >= 0) {
                    conditions.add(TRANSACTION.SUBTYPE.eq(subtype))
                }
            }
            if (height < Integer.MAX_VALUE) {
                conditions.add(TRANSACTION.HEIGHT.le(height))
            }

            var select = ctx.selectFrom(TRANSACTION).where(conditions).and(
                    TRANSACTION.RECIPIENT_ID.eq(account.id).and(
                            TRANSACTION.SENDER_ID.ne(account.id)
                    )
            ).unionAll(
                    ctx.selectFrom(TRANSACTION).where(conditions).and(
                            TRANSACTION.SENDER_ID.eq(account.id)
                    )
            )

            if (includeIndirectIncoming) {
                select = select.unionAll(ctx.selectFrom(TRANSACTION)
                        .where(conditions)
                        .and(TRANSACTION.ID.`in`(dp.indirectIncomingStore.getIndirectIncomings(account.id, from, to))))
            }

            val selectQuery = select
                    .orderBy(TRANSACTION.BLOCK_TIMESTAMP.desc(), TRANSACTION.ID.desc())
                    .query

            DbUtils.applyLimits(selectQuery, from, to)

            getTransactions(ctx, selectQuery.fetch())
        }
    }

    override fun getTransactions(ctx: DSLContext, rs: Result<TransactionRecord>): Collection<Transaction> {
        return rs.inlineMap { r ->
            try {
                return@inlineMap dp.transactionDb.loadTransaction(r)
            } catch (e: BurstException.ValidationException) {
                throw RuntimeException(e)
            }
        }
    }

    override fun addBlock(block: Block) {
        dp.db.useDslContext { ctx -> dp.blockDb.saveBlock(ctx, block) }
    }

    override fun getLatestBlocks(amountBlocks: Int): Collection<Block> {
        val latestBlockHeight = dp.blockDb.findLastBlock()!!.height

        val firstLatestBlockHeight = max(0, latestBlockHeight - amountBlocks)

        return dp.db.getUsingDslContext<Collection<Block>> { ctx ->
            getBlocks(ctx.selectFrom(BLOCK)
                    .where(BLOCK.HEIGHT.between(firstLatestBlockHeight).and(latestBlockHeight))
                    .orderBy(BLOCK.HEIGHT.asc())
                    .fetch())
        }
    }
}
