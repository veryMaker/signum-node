package brs

import brs.peer.Peer
import brs.util.Observable
import com.google.gson.JsonObject
import kotlinx.coroutines.sync.Mutex

interface TransactionProcessor : Observable<Collection<Transaction>, TransactionProcessor.Event> {

    @Deprecated("Just use UTStore directly")
    val allUnconfirmedTransactions: Collection<Transaction>

    @Deprecated("Just use UTStore directly")
    val amountUnconfirmedTransactions: Int

    val unconfirmedTransactionsSyncObj: Mutex

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

    fun broadcast(transaction: Transaction): Int?

    fun processPeerTransactions(request: JsonObject, peer: Peer)

    fun parseTransaction(bytes: ByteArray): Transaction

    fun parseTransaction(json: JsonObject): Transaction

    fun newTransactionBuilder(senderPublicKey: ByteArray, amountNQT: Long, feeNQT: Long, deadline: Short, attachment: Attachment): Transaction.Builder

    fun getTransactionVersion(blockHeight: Int): Int

    fun processLater(transactions: Collection<Transaction>)

    @Deprecated("Just use UTStore directly")
    fun requeueAllUnconfirmedTransactions()

    @Deprecated("Just use UTStore directly")
    fun removeForgedTransactions(transactions: Collection<Transaction>)

    fun notifyListeners(transactions: Collection<Transaction>, eventType: Event)
}
