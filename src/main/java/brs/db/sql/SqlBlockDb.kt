package brs.db.sql

import brs.Block
import brs.BurstException
import brs.DependencyProvider
import brs.db.BlockDb
import brs.schema.Tables.BLOCK
import brs.schema.tables.records.BlockRecord
import brs.util.logging.safeInfo
import brs.util.logging.safeTrace
import org.jooq.DSLContext
import org.jooq.impl.TableImpl
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.util.*

class SqlBlockDb(private val dp: DependencyProvider) : BlockDb {

    override fun findBlock(blockId: Long): Block? {
        return dp.db.getUsingDslContext<Block?> { ctx ->
            try {
                val r = ctx.selectFrom(BLOCK).where(BLOCK.ID.eq(blockId)).fetchAny()
                return@getUsingDslContext if (r == null) null else loadBlock(r)
            } catch (e: BurstException.ValidationException) {
                throw RuntimeException("Block already in database, id = $blockId, does not pass validation!", e)
            }
        }
    }

    override fun hasBlock(blockId: Long): Boolean {
        return dp.db.getUsingDslContext { ctx -> ctx.fetchExists(ctx.selectOne().from(BLOCK).where(BLOCK.ID.eq(blockId))) }
    }

    override fun findBlockIdAtHeight(height: Int): Long {
        return dp.db.getUsingDslContext<Long> { ctx ->
            val id = ctx.select(BLOCK.ID).from(BLOCK).where(BLOCK.HEIGHT.eq(height)).fetchOne(BLOCK.ID)
                    ?: throw RuntimeException("Block at height $height not found in database!")
            id
        }
    }

    override fun findBlockAtHeight(height: Int): Block {
        return dp.db.getUsingDslContext<Block> { ctx ->
            try {
                return@getUsingDslContext loadBlock(ctx.selectFrom(BLOCK).where(BLOCK.HEIGHT.eq(height)).fetchAny() ?: throw RuntimeException("Block at height $height not found in database!"))
            } catch (e: BurstException.ValidationException) {
                throw RuntimeException(e.toString(), e)
            }
        }
    }

    override fun findLastBlock(): Block {
        return dp.db.getUsingDslContext<Block> { ctx ->
            try {
                return@getUsingDslContext loadBlock(ctx.selectFrom(BLOCK)
                        .orderBy(BLOCK.DB_ID.desc())
                        .limit(1)
                        .fetchAny())
            } catch (e: BurstException.ValidationException) {
                throw RuntimeException("Last block already in database does not pass validation!", e)
            }
        }
    }

    override fun findLastBlock(timestamp: Int): Block {
        return dp.db.getUsingDslContext<Block> { ctx ->
            try {
                return@getUsingDslContext loadBlock(ctx.selectFrom(BLOCK)
                        .where(BLOCK.TIMESTAMP.lessOrEqual(timestamp))
                        .orderBy(BLOCK.DB_ID.desc())
                        .limit(1)
                        .fetchAny())
            } catch (e: BurstException.ValidationException) {
                throw RuntimeException("Block already in database at timestamp $timestamp does not pass validation!", e)
            }
        }
    }

    override fun loadBlock(r: BlockRecord): Block {
        val version = r.version!!
        val timestamp = r.timestamp!!
        val previousBlockId = Optional.ofNullable(r.previousBlockId).orElse(0L)
        val totalAmountNQT = r.totalAmount!!
        val totalFeeNQT = r.totalFee!!
        val payloadLength = r.payloadLength!!
        val generatorPublicKey = r.generatorPublicKey
        val previousBlockHash = r.previousBlockHash
        val cumulativeDifficulty = BigInteger(r.cumulativeDifficulty)
        val baseTarget = r.baseTarget!!
        val nextBlockId = Optional.ofNullable(r.nextBlockId).orElse(0L)
        val height = r.height!!
        val generationSignature = r.generationSignature
        val blockSignature = r.blockSignature
        val payloadHash = r.payloadHash
        val id = r.id!!
        val nonce = r.nonce!!
        val blockATs = r.ats

        return Block(dp, version, timestamp, previousBlockId, totalAmountNQT, totalFeeNQT, payloadLength, payloadHash,
                generatorPublicKey, generationSignature, blockSignature, previousBlockHash,
                cumulativeDifficulty, baseTarget, nextBlockId, height, id, nonce, blockATs)
    }

    override fun saveBlock(ctx: DSLContext, block: Block) {
        ctx.insertInto(BLOCK, BLOCK.ID, BLOCK.VERSION, BLOCK.TIMESTAMP, BLOCK.PREVIOUS_BLOCK_ID,
                BLOCK.TOTAL_AMOUNT, BLOCK.TOTAL_FEE, BLOCK.PAYLOAD_LENGTH, BLOCK.GENERATOR_PUBLIC_KEY,
                BLOCK.PREVIOUS_BLOCK_HASH, BLOCK.CUMULATIVE_DIFFICULTY, BLOCK.BASE_TARGET, BLOCK.HEIGHT,
                BLOCK.GENERATION_SIGNATURE, BLOCK.BLOCK_SIGNATURE, BLOCK.PAYLOAD_HASH, BLOCK.GENERATOR_ID,
                BLOCK.NONCE, BLOCK.ATS)
                .values(block.id, block.version, block.timestamp,
                        if (block.previousBlockId == 0L) null else block.previousBlockId,
                        block.totalAmountNQT, block.totalFeeNQT, block.payloadLength,
                        block.generatorPublicKey, block.previousBlockHash,
                        block.cumulativeDifficulty.toByteArray(), block.baseTarget, block.height,
                        block.generationSignature, block.blockSignature, block.payloadHash,
                        block.generatorId, block.nonce, block.blockATs)
                .execute()

        dp.transactionDb.saveTransactions(block.getTransactions())

        if (block.previousBlockId != 0L) {
            ctx.update(BLOCK)
                    .set(BLOCK.NEXT_BLOCK_ID, block.id)
                    .where(BLOCK.ID.eq(block.previousBlockId))
                    .execute()
        }
    }

    // relying on cascade triggers in the database to delete the transactions for all deleted blocks
    override fun deleteBlocksFrom(blockId: Long) {
        if (!dp.db.isInTransaction()) {
            try {
                dp.db.beginTransaction()
                deleteBlocksFrom(blockId)
                dp.db.commitTransaction()
            } catch (e: Exception) {
                dp.db.rollbackTransaction()
                throw e
            } finally {
                dp.db.endTransaction()
            }
            return
        }
        dp.db.useDslContext { ctx ->
            val blockHeightQuery = ctx.selectQuery()
            blockHeightQuery.addFrom(BLOCK)
            blockHeightQuery.addSelect(BLOCK.HEIGHT)
            blockHeightQuery.addConditions(BLOCK.ID.eq(blockId))
            val blockHeight = blockHeightQuery.fetchOne().get(BLOCK.HEIGHT)

            if (blockHeight != null) {
                val deleteQuery = ctx.deleteQuery(BLOCK)
                deleteQuery.addConditions(BLOCK.HEIGHT.ge(blockHeight))
                deleteQuery.execute()
            }
        }
    }

    override fun deleteAll(force: Boolean) {
        if (!dp.db.isInTransaction()) {
            try {
                dp.db.beginTransaction()
                deleteAll(force)
                dp.db.commitTransaction()
            } catch (e: Exception) {
                dp.db.rollbackTransaction()
                throw e
            }

            dp.db.endTransaction()
            return
        }
        logger.safeInfo { "Deleting blockchain..." }
        dp.db.useDslContext { ctx ->
            val tables = listOf<TableImpl<*>>(brs.schema.Tables.ACCOUNT,
                    brs.schema.Tables.ACCOUNT_ASSET, brs.schema.Tables.ALIAS, brs.schema.Tables.ALIAS_OFFER,
                    brs.schema.Tables.ASK_ORDER, brs.schema.Tables.ASSET, brs.schema.Tables.ASSET_TRANSFER,
                    brs.schema.Tables.AT, brs.schema.Tables.AT_STATE, brs.schema.Tables.BID_ORDER,
                    BLOCK, brs.schema.Tables.ESCROW, brs.schema.Tables.ESCROW_DECISION,
                    brs.schema.Tables.GOODS, brs.schema.Tables.PEER, brs.schema.Tables.PURCHASE,
                    brs.schema.Tables.PURCHASE_FEEDBACK, brs.schema.Tables.PURCHASE_PUBLIC_FEEDBACK,
                    brs.schema.Tables.REWARD_RECIP_ASSIGN, brs.schema.Tables.SUBSCRIPTION,
                    brs.schema.Tables.TRADE, brs.schema.Tables.TRANSACTION,
                    brs.schema.Tables.UNCONFIRMED_TRANSACTION)
            for (table in tables) {
                try {
                    ctx.truncate(table).execute()
                } catch (e: org.jooq.exception.DataAccessException) {
                    if (force) {
                        logger.safeTrace(e) { "exception during truncate $table" }
                    } else {
                        throw e
                    }
                }

            }
        }
    }

    override fun optimize() {
        dp.db.optimizeTable(BLOCK.name)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BlockDb::class.java)
    }
}
