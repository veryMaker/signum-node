package brs.services

import brs.entity.Transaction
import brs.peer.Peer

interface UnconfirmedTransactionService {
    val all: List<Transaction>

    val amount: Int

    fun put(transaction: Transaction, peer: Peer?): Boolean

    fun get(transactionId: Long?): Transaction?

    fun exists(transactionId: Long?): Boolean

    fun getAllFor(peer: Peer): Collection<Transaction>

    fun remove(transaction: Transaction)

    fun clear()

    /**
     * Review which transactions are still eligible to stay
     * @return The list of removed transactions
     * TODO rename method
     */
    fun resetAccountBalances()

    fun markFingerPrintsOf(peer: Peer?, transactions: Collection<Transaction>)

    fun removeForgedTransactions(transactions: Collection<Transaction>)
}
