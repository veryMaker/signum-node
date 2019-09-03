package brs.db

import brs.BurstException
import brs.Transaction
import brs.schema.tables.records.TransactionRecord

interface TransactionDb : Table {
    fun findTransaction(transactionId: Long): Transaction?

    fun findTransactionByFullHash(fullHash: String): Transaction?  // TODO add byte[] method

    fun hasTransaction(transactionId: Long): Boolean

    fun hasTransactionByFullHash(fullHash: String): Boolean  // TODO add byte[] method

    fun findBlockTransactions(blockId: Long): List<Transaction>

    fun saveTransactions(transactions: List<Transaction>)
}
