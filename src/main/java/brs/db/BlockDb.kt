package brs.db

import brs.Block
import brs.schema.tables.records.BlockRecord
import org.jooq.DSLContext

interface BlockDb : Table {
    suspend fun findBlock(blockId: Long): Block?

    suspend fun hasBlock(blockId: Long): Boolean

    suspend fun findBlockIdAtHeight(height: Int): Long

    suspend fun findBlockAtHeight(height: Int): Block

    suspend fun findLastBlock(): Block?

    suspend fun findLastBlock(timestamp: Int): Block?

    fun loadBlock(r: BlockRecord): Block

    suspend fun saveBlock(ctx: DSLContext, block: Block)

    // relying on cascade triggers in the database to delete the transactions for all deleted blocks
    suspend fun deleteBlocksFrom(blockId: Long)

    suspend fun deleteAll(force: Boolean)
}
