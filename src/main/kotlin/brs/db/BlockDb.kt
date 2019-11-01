package brs.db

import brs.entity.Block
import brs.schema.tables.records.BlockRecord
import org.jooq.DSLContext

interface BlockDb : Table {
    /**
     * TODO
     */
    fun findBlock(blockId: Long): Block?

    /**
     * TODO
     */
    fun hasBlock(blockId: Long): Boolean

    /**
     * TODO
     */
    fun findBlockIdAtHeight(height: Int): Long

    /**
     * TODO
     */
    fun findBlockAtHeight(height: Int): Block

    /**
     * TODO
     */
    fun findLastBlock(): Block?

    /**
     * TODO
     */
    fun findLastBlock(timestamp: Int): Block?

    /**
     * TODO
     */
    fun loadBlock(r: BlockRecord): Block

    /**
     * TODO
     */
    fun saveBlock(ctx: DSLContext, block: Block)

    // relying on cascade triggers in the database to delete the transactions for all deleted blocks
    /**
     * TODO
     */
    fun deleteBlocksFrom(blockId: Long)

    /**
     * TODO
     */
    fun deleteAll(force: Boolean)
}
