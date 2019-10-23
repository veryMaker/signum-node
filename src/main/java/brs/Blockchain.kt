package brs

interface Blockchain {

    var lastBlock: Block

    val height: Int

    suspend fun getTransactionCount(): Int

    suspend fun getAllTransactions(): Collection<Transaction>

    suspend fun getLastBlock(timestamp: Int): Block?

    suspend fun getBlock(blockId: Long): Block?

    suspend fun getBlockAtHeight(height: Int): Block?

    suspend fun hasBlock(blockId: Long): Boolean

    suspend fun getBlocks(from: Int, to: Int): Collection<Block>

    suspend fun getBlocks(account: Account, timestamp: Int): Collection<Block>

    suspend fun getBlocks(account: Account, timestamp: Int, from: Int, to: Int): Collection<Block>

    suspend fun getBlockIdsAfter(blockId: Long, limit: Int): Collection<Long>

    suspend fun getBlocksAfter(blockId: Long, limit: Int): Collection<Block>

    suspend fun getBlockIdAtHeight(height: Int): Long

    suspend fun getTransaction(transactionId: Long): Transaction?

    suspend fun getTransactionByFullHash(fullHash: ByteArray): Transaction?

    suspend fun hasTransaction(transactionId: Long): Boolean

    suspend fun hasTransactionByFullHash(fullHash: ByteArray): Boolean

    suspend fun getTransactions(account: Account, type: Byte, subtype: Byte, blockTimestamp: Int, includeIndirectIncoming: Boolean): Collection<Transaction>

    suspend fun getTransactions(account: Account, numberOfConfirmations: Int, type: Byte, subtype: Byte, blockTimestamp: Int, from: Int, to: Int, includeIndirectIncoming: Boolean): Collection<Transaction>

    fun setLastBlock(previousBlock: Block, block: Block)
}
