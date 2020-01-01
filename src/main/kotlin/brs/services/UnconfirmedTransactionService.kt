package brs.services

import brs.entity.Transaction
import brs.peer.Peer

interface UnconfirmedTransactionService {
    /**
     * TODO
     */
    val all: List<Transaction>

    /**
     * TODO
     */
    val amount: Int

    /**
     * TODO
     */
    fun put(transaction: Transaction, peer: Peer?): Boolean

    /**
     * TODO
     */
    fun get(transactionId: Long?): Transaction?

    /**
     * TODO
     */
    fun exists(transactionId: Long?): Boolean

    /**
     * TODO
     */
    fun getAllFor(peer: Peer): Collection<Transaction>

    /**
     * TODO
     */
    fun remove(transaction: Transaction)

    /**
     * TODO
     */
    fun clear()

    /**
     * Review which transactions are still eligible to stay
     * @return The list of removed transactions
     * TODO rename method
     */
    fun resetAccountBalances()

    /**
     * TODO
     */
    fun markFingerPrintsOf(peer: Peer?, transaction: Transaction)

    /**
     * TODO
     */
    fun markFingerPrintsOf(peer: Peer?, transactions: Collection<Transaction>)

    /**
     * TODO
     */
    fun removeForgedTransactions(transactions: Collection<Transaction>)
}
