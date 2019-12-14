package brs.db.sql

import brs.db.BlockDb
import brs.db.transaction
import brs.db.useDslContext
import brs.entity.Block
import brs.entity.DependencyProvider
import brs.schema.Tables.BLOCK
import brs.schema.tables.records.BlockRecord
import brs.util.BurstException
import org.jooq.DSLContext
import java.math.BigInteger

internal class SqlBlockDb(private val dp: DependencyProvider) : BlockDb {
    override fun findBlock(blockId: Long): Block? {
        return dp.db.useDslContext { ctx ->
            try {
                val r = ctx.selectFrom(BLOCK).where(BLOCK.ID.eq(blockId)).fetchAny()
                return@useDslContext if (r == null) null else loadBlock(r)
            } catch (e: BurstException.ValidationException) {
                throw Exception("Block already in database, id = $blockId, does not pass validation!", e)
            }
        }
    }

    override fun hasBlock(blockId: Long): Boolean {
        return dp.db.useDslContext { ctx -> ctx.fetchExists(ctx.selectOne().from(BLOCK).where(BLOCK.ID.eq(blockId))) }
    }

    override fun findBlockIdAtHeight(height: Int): Long {
        return dp.db.useDslContext { ctx ->
            val id = ctx.select(BLOCK.ID).from(BLOCK).where(BLOCK.HEIGHT.eq(height)).fetchOne(BLOCK.ID)
                ?: throw Exception("Block at height $height not found in database!")
            id
        }
    }

    override fun findBlockAtHeight(height: Int): Block {
        return dp.db.useDslContext { ctx ->
            loadBlock(
                ctx.selectFrom(BLOCK).where(BLOCK.HEIGHT.eq(height)).fetchAny()
                    ?: throw Exception("Block at height $height not found in database!")
            )
        }
    }

    override fun findLastBlock(): Block {
        return dp.db.useDslContext { ctx ->
            try {
                return@useDslContext loadBlock(
                    ctx.selectFrom(BLOCK)
                        .orderBy(BLOCK.DB_ID.desc())
                        .limit(1)
                        .fetchAny()
                )
            } catch (e: BurstException.ValidationException) {
                throw Exception("Last block already in database does not pass validation!", e)
            }
        }
    }

    override fun findLastBlock(timestamp: Int): Block {
        return dp.db.useDslContext { ctx ->
            try {
                return@useDslContext loadBlock(
                    ctx.selectFrom(BLOCK)
                        .where(BLOCK.TIMESTAMP.lessOrEqual(timestamp))
                        .orderBy(BLOCK.DB_ID.desc())
                        .limit(1)
                        .fetchAny()
                )
            } catch (e: BurstException.ValidationException) {
                throw Exception("Block already in database at timestamp $timestamp does not pass validation!", e)
            }
        }
    }

    override fun loadBlock(record: BlockRecord): Block {
        val version = record.version
        val timestamp = record.timestamp
        val previousBlockId = record.previousBlockId ?: 0L
        val totalAmountPlanck = record.totalAmount
        val totalFeePlanck = record.totalFee
        val payloadLength = record.payloadLength
        val generatorPublicKey = record.generatorPublicKey
        val previousBlockHash = record.previousBlockHash
        val cumulativeDifficulty = BigInteger(record.cumulativeDifficulty)
        val baseTarget = record.baseTarget
        val nextBlockId = record.nextBlockId ?: 0L
        val height = record.height
        val generationSignature = record.generationSignature
        val blockSignature = record.blockSignature
        val payloadHash = record.payloadHash
        val id = record.id
        val nonce = record.nonce
        val blockATs = record.ats

        return Block(
            dp, version, timestamp, previousBlockId, totalAmountPlanck, totalFeePlanck, payloadLength, payloadHash,
            generatorPublicKey, generationSignature, blockSignature, previousBlockHash,
            cumulativeDifficulty, baseTarget, nextBlockId, height, id, nonce, blockATs
        )
    }

    override fun saveBlock(ctx: DSLContext, block: Block) {
        ctx.insertInto(
            BLOCK, BLOCK.ID, BLOCK.VERSION, BLOCK.TIMESTAMP, BLOCK.PREVIOUS_BLOCK_ID,
            BLOCK.TOTAL_AMOUNT, BLOCK.TOTAL_FEE, BLOCK.PAYLOAD_LENGTH, BLOCK.GENERATOR_PUBLIC_KEY,
            BLOCK.PREVIOUS_BLOCK_HASH, BLOCK.CUMULATIVE_DIFFICULTY, BLOCK.BASE_TARGET, BLOCK.HEIGHT,
            BLOCK.GENERATION_SIGNATURE, BLOCK.BLOCK_SIGNATURE, BLOCK.PAYLOAD_HASH, BLOCK.GENERATOR_ID,
            BLOCK.NONCE, BLOCK.ATS
        )
            .values(
                block.id, block.version, block.timestamp,
                if (block.previousBlockId == 0L) null else block.previousBlockId,
                block.totalAmountPlanck, block.totalFeePlanck, block.payloadLength,
                block.generatorPublicKey, block.previousBlockHash,
                block.cumulativeDifficulty.toByteArray(), block.baseTarget, block.height,
                block.generationSignature, block.blockSignature, block.payloadHash,
                block.generatorId, block.nonce, block.blockATs
            )
            .execute()

        dp.transactionDb.saveTransactions(block.transactions)

        if (block.previousBlockId != 0L) {
            ctx.update(BLOCK)
                .set(BLOCK.NEXT_BLOCK_ID, block.id)
                .where(BLOCK.ID.eq(block.previousBlockId))
                .execute()
        }
    }

    override fun deleteBlocksFrom(blockId: Long) {
        // This relies on cascade triggers in the database to delete the transactions in deleted blocks
        if (!dp.db.isInTransaction()) {
            dp.db.transaction {
                deleteBlocksFrom(blockId)
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

    override fun optimize() {
        dp.db.optimizeTable(BLOCK.name)
    }
}
