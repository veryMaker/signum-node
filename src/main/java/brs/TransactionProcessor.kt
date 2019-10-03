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
    suspend fun revalidateUnconfirmedTransactions()

    @Deprecated("Just use UTStore directly")
    suspend fun getAllUnconfirmedTransactionsFor(peer: Peer): Collection<Transaction>

    @Deprecated("Just use UTStore directly")
    suspend fun markFingerPrintsOf(peer: Peer, transactions: Collection<Transaction>)

    @Deprecated("Just use UTStore directly")
    suspend fun getUnconfirmedTransaction(transactionId: Long): Transaction?

    @Deprecated("Just use UTStore directly")
    suspend fun clearUnconfirmedTransactions()

    suspend fun broadcast(transaction: Transaction): Int?

    suspend fun processPeerTransactions(request: JsonObject, peer: Peer)

    fun parseTransaction(bytes: ByteArray): Transaction

    fun parseTransaction(json: JsonObject): Transaction

    fun newTransactionBuilder(senderPublicKey: ByteArray, amountNQT: Long, feeNQT: Long, deadline: Short, attachment: Attachment): Transaction.Builder

    fun getTransactionVersion(blockHeight: Int): Int

    suspend fun processLater(transactions: Collection<Transaction>)

    @Deprecated("Just use UTStore directly")
    suspend fun requeueAllUnconfirmedTransactions()

    @Deprecated("Just use UTStore directly")
    suspend fun removeForgedTransactions(transactions: Collection<Transaction>)

    suspend fun notifyListeners(transactions: Collection<Transaction>, eventType: Event)
}
