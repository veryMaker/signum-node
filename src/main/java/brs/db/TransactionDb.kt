package brs.db

import brs.Transaction
import brs.schema.tables.records.TransactionRecord

interface TransactionDb : Table {
    suspend fun findTransaction(transactionId: Long): Transaction?

    suspend fun findTransactionByFullHash(fullHash: ByteArray): Transaction?

    suspend fun hasTransaction(transactionId: Long): Boolean

    suspend fun hasTransactionByFullHash(fullHash: ByteArray): Boolean

    suspend fun findBlockTransactions(blockId: Long): Collection<Transaction>

    suspend fun saveTransactions(transactions: Collection<Transaction>)
    fun loadTransaction(tr: TransactionRecord): Transaction
}
