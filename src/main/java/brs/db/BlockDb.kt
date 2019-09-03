package brs.db

import brs.Block
import brs.BurstException
import brs.schema.tables.records.BlockRecord
import org.jooq.DSLContext

interface BlockDb : Table {
    fun findBlock(blockId: Long): Block?

    fun hasBlock(blockId: Long): Boolean

    fun findBlockIdAtHeight(height: Int): Long

    fun findBlockAtHeight(height: Int): Block

    fun findLastBlock(): Block?

    fun findLastBlock(timestamp: Int): Block?

    @Throws(BurstException.ValidationException::class)
    fun loadBlock(r: BlockRecord): Block?

    fun saveBlock(ctx: DSLContext, block: Block)

    // relying on cascade triggers in the database to delete the transactions for all deleted blocks
    fun deleteBlocksFrom(blockId: Long)

    fun deleteAll(force: Boolean)
}
