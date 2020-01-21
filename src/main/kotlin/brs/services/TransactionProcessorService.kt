package brs.services

import brs.entity.Transaction
import brs.peer.Peer
import brs.transaction.appendix.Attachment
import brs.util.Observable

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
    fun processPeerTransactions(transactions: Collection<Transaction>, peer: Peer, rebroadcast: Boolean = true): Collection<Transaction>

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
