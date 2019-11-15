package brs.db

import brs.entity.Transaction
import brs.schema.tables.records.TransactionRecord

interface TransactionDb : Table {
    /**
     * TODO
     */
    fun findTransaction(transactionId: Long): Transaction?

    /**
     * TODO
     */
    fun findTransactionByFullHash(fullHash: ByteArray): Transaction?

    /**
     * TODO
     */
    fun hasTransaction(transactionId: Long): Boolean

    /**
     * TODO
     */
    fun hasTransactionByFullHash(fullHash: ByteArray): Boolean

    /**
     * TODO
     */
    fun findBlockTransactions(blockId: Long): Collection<Transaction>

    /**
     * TODO
     */
    fun saveTransactions(transactions: Collection<Transaction>)

    /**
     * TODO
     */
    fun loadTransaction(tr: TransactionRecord): Transaction
}
