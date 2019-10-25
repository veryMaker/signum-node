package brs.db.store

import brs.Transaction
import brs.db.BurstKey
import brs.db.sql.EntitySqlTable

interface TransactionProcessorStore {

    val unconfirmedTransactionDbKeyFactory: BurstKey.LongKeyFactory<Transaction>

    val lostTransactions: Set<Transaction>

    val lostTransactionHeights: Map<Long, Int>

    val unconfirmedTransactionTable: EntitySqlTable<Transaction>
    // WATCH: BUSINESS-LOGIC
    fun processLater(transactions: Collection<Transaction>)

    fun deleteTransaction(transaction: Transaction): Int

    fun hasTransaction(transactionId: Long): Boolean
}
