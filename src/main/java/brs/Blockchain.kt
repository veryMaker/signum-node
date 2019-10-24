package brs

interface Blockchain {

    var lastBlock: Block

    val height: Int

    fun getTransactionCount(): Int

    fun getAllTransactions(): Collection<Transaction>

    fun getLastBlock(timestamp: Int): Block?

    fun getBlock(blockId: Long): Block?

    fun getBlockAtHeight(height: Int): Block?

    fun hasBlock(blockId: Long): Boolean

    fun getBlocks(from: Int, to: Int): Collection<Block>

    fun getBlocks(account: Account, timestamp: Int): Collection<Block>

    fun getBlocks(account: Account, timestamp: Int, from: Int, to: Int): Collection<Block>

    fun getBlockIdsAfter(blockId: Long, limit: Int): Collection<Long>

    fun getBlocksAfter(blockId: Long, limit: Int): Collection<Block>

    fun getBlockIdAtHeight(height: Int): Long

    fun getTransaction(transactionId: Long): Transaction?

    fun getTransactionByFullHash(fullHash: ByteArray): Transaction?

    fun hasTransaction(transactionId: Long): Boolean

    fun hasTransactionByFullHash(fullHash: ByteArray): Boolean

    fun getTransactions(account: Account, type: Byte, subtype: Byte, blockTimestamp: Int, includeIndirectIncoming: Boolean): Collection<Transaction>

    fun getTransactions(account: Account, numberOfConfirmations: Int, type: Byte, subtype: Byte, blockTimestamp: Int, from: Int, to: Int, includeIndirectIncoming: Boolean): Collection<Transaction>

    fun setLastBlock(previousBlock: Block, block: Block)
}
