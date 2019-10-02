package brs.unconfirmedtransactions

import brs.Transaction
import brs.peer.Peer

interface UnconfirmedTransactionStore {
    val all: List<Transaction>

    val amount: Int

    suspend fun put(transaction: Transaction, peer: Peer?): Boolean

    suspend fun get(transactionId: Long?): Transaction?

    suspend fun exists(transactionId: Long?): Boolean

    suspend fun getAllFor(peer: Peer): Collection<Transaction>

    suspend fun remove(transaction: Transaction)

    suspend fun clear()

    /**
     * Review which transactions are still eligible to stay
     * @return The list of removed transactions
     * TODO rename method
     */
    suspend fun resetAccountBalances()

    suspend fun markFingerPrintsOf(peer: Peer?, transactions: Collection<Transaction>)

    suspend fun removeForgedTransactions(transactions: Collection<Transaction>)
}
