package brs.services

import brs.entity.Transaction
import brs.peer.Peer
import brs.transaction.appendix.Attachment
import brs.util.Observable
import com.google.gson.JsonObject

interface TransactionProcessorService : Observable<Collection<Transaction>, TransactionProcessorService.Event> {
    /**
     * TODO
     */
    @Deprecated("Just use UTStore directly")
    val allUnconfirmedTransactions: Collection<Transaction>

    /**
     * TODO
     */
    @Deprecated("Just use UTStore directly")
    val amountUnconfirmedTransactions: Int

    /**
     * TODO
     */
    enum class Event {
        REMOVED_UNCONFIRMED_TRANSACTIONS,
        ADDED_UNCONFIRMED_TRANSACTIONS,
        ADDED_CONFIRMED_TRANSACTIONS,
    }

    /**
     * TODO
     */
    @Deprecated("Just use UTStore directly")
    fun revalidateUnconfirmedTransactions()

    /**
     * TODO
     */
    @Deprecated("Just use UTStore directly")
    fun getAllUnconfirmedTransactionsFor(peer: Peer): Collection<Transaction>

    /**
     * TODO
     */
    @Deprecated("Just use UTStore directly")
    fun markFingerPrintsOf(peer: Peer, transactions: Collection<Transaction>)

    /**
     * TODO
     */
    @Deprecated("Just use UTStore directly")
    fun getUnconfirmedTransaction(transactionId: Long): Transaction?

    /**
     * TODO
     */
    @Deprecated("Just use UTStore directly")
    fun clearUnconfirmedTransactions()

    /**
     * TODO
     */
    fun broadcast(transaction: Transaction): Int?

    /**
     * TODO
     */
    fun processPeerTransactions(request: JsonObject, peer: Peer)

    /**
     * TODO
     */
    fun parseTransaction(bytes: ByteArray): Transaction

    /**
     * TODO
     */
    fun parseTransaction(json: JsonObject): Transaction

    /**
     * TODO
     */
    fun newTransactionBuilder(
        senderPublicKey: ByteArray,
        amountPlanck: Long,
        feePlanck: Long,
        deadline: Short,
        attachment: Attachment
    ): Transaction.Builder

    /**
     * TODO
     */
    fun getTransactionVersion(blockHeight: Int): Int

    /**
     * TODO
     */
    fun processLater(transactions: Collection<Transaction>)

    /**
     * TODO
     */
    @Deprecated("Just use UTStore directly")
    fun requeueAllUnconfirmedTransactions()

    /**
     * TODO
     */
    @Deprecated("Just use UTStore directly")
    fun removeForgedTransactions(transactions: Collection<Transaction>)

    /**
     * TODO
     */
    fun notifyListeners(transactions: Collection<Transaction>, eventType: Event)
}
