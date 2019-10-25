package brs.services

import brs.entity.Block
import brs.util.sync.Mutex
import java.math.BigInteger

interface DownloadCacheService {
    val mutex: Mutex
    val isFull: Boolean
    val cumulativeDifficulty: BigInteger
    val firstUnverifiedBlock: Block?
    val unverifiedSize: Int
    val blockCacheSize: Int
    val forkList: List<Block>
    val lastBlock: Block?

    fun lockCache()
    fun unlockCache()
    fun getUnverifiedBlockIdFromPos(pos: Int): Long
    fun removeUnverified(blockId: Long)
    fun removeUnverifiedBatch(blocks: Collection<Block>)
    fun resetCache()
    fun getBlock(blockId: Long): Block?
    fun getNextBlock(prevBlockId: Long): Block?
    fun hasBlock(blockId: Long): Boolean
    fun canBeFork(oldBlockId: Long): Boolean
    fun addBlock(block: Block): Boolean
    fun addForkBlock(block: Block)
    fun resetForkBlocks()
    fun removeBlock(block: Block)
    fun getPoCVersion(blockId: Long): Int
    fun getLastBlockId(): Long
    fun size(): Int
}