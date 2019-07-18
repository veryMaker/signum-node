package brs

import brs.peer.Peer
import brs.util.Observable
import com.google.gson.JsonObject

interface TransactionProcessor : Observable<List<Transaction>, TransactionProcessor.Event> {

    val allUnconfirmedTransactions: List<Transaction>

    val amountUnconfirmedTransactions: Int

    enum class Event {
        REMOVED_UNCONFIRMED_TRANSACTIONS,
        ADDED_UNCONFIRMED_TRANSACTIONS,
        ADDED_CONFIRMED_TRANSACTIONS,
        ADDED_DOUBLESPENDING_TRANSACTIONS
    }

    fun getAllUnconfirmedTransactionsFor(peer: Peer): List<Transaction>

    fun markFingerPrintsOf(peer: Peer, transactions: List<Transaction>)

    fun getUnconfirmedTransaction(transactionId: Long): Transaction

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
}
