package brs

import brs.peer.Peer
import brs.util.Observable
import com.google.gson.JsonObject

interface TransactionProcessor : Observable<Collection<Transaction>, TransactionProcessor.Event> {

    @Deprecated("Just use UTStore directly")
    val allUnconfirmedTransactions: Collection<Transaction>

    @Deprecated("Just use UTStore directly")
    val amountUnconfirmedTransactions: Int

    enum class Event {
        REMOVED_UNCONFIRMED_TRANSACTIONS,
        ADDED_UNCONFIRMED_TRANSACTIONS,
        ADDED_CONFIRMED_TRANSACTIONS,
    }

    @Deprecated("Just use UTStore directly")
    fun revalidateUnconfirmedTransactions()

    @Deprecated("Just use UTStore directly")
    fun getAllUnconfirmedTransactionsFor(peer: Peer): Collection<Transaction>

    @Deprecated("Just use UTStore directly")
    fun markFingerPrintsOf(peer: Peer, transactions: Collection<Transaction>)

    @Deprecated("Just use UTStore directly")
    fun getUnconfirmedTransaction(transactionId: Long): Transaction?

    @Deprecated("Just use UTStore directly")
    fun clearUnconfirmedTransactions()

    @Throws(BurstException.ValidationException::class)
    fun broadcast(transaction: Transaction): Int?

    @Throws(BurstException.ValidationException::class)
    fun processPeerTransactions(request: JsonObject, peer: Peer)

    @Throws(BurstException.ValidationException::class)
    fun parseTransaction(bytes: ByteArray): Transaction

    @Throws(BurstException.ValidationException::class)
    fun parseTransaction(json: JsonObject): Transaction

    fun newTransactionBuilder(senderPublicKey: ByteArray, amountNQT: Long, feeNQT: Long, deadline: Short, attachment: Attachment): Transaction.Builder

    fun getTransactionVersion(blockHeight: Int): Int

    fun processLater(transactions: Collection<Transaction>)

    fun getUnconfirmedTransactionsSyncObj(): Any

    @Deprecated("Just use UTStore directly")
    fun requeueAllUnconfirmedTransactions()

    @Deprecated("Just use UTStore directly")
    fun removeForgedTransactions(transactions: Collection<Transaction>)

    fun notifyListeners(transactions: Collection<Transaction>, eventType: Event)
}
