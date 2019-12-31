package brs.services

import brs.entity.Transaction
import brs.peer.Peer
import brs.transaction.appendix.Attachment
import brs.util.Observable
import brs.util.jetty.get
import com.google.gson.JsonObject

interface TransactionProcessorService : Observable<Collection<Transaction>, TransactionProcessorService.Event> {

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
    fun revalidateUnconfirmedTransactions()

    /**
     * TODO
     */
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
    fun notifyListeners(transactions: Collection<Transaction>, eventType: Event)
}
