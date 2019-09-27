package brs.db

import brs.Transaction
import brs.schema.tables.records.TransactionRecord

interface TransactionDb : Table {
    fun findTransaction(transactionId: Long): Transaction?

    fun findTransactionByFullHash(fullHash: ByteArray): Transaction?

    fun hasTransaction(transactionId: Long): Boolean

    fun hasTransactionByFullHash(fullHash: ByteArray): Boolean

    fun findBlockTransactions(blockId: Long): Collection<Transaction>

    fun saveTransactions(transactions: Collection<Transaction>)
    fun loadTransaction(tr: TransactionRecord): Transaction
}
