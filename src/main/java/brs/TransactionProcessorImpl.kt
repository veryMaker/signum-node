package brs

import brs.BurstException.ValidationException
import brs.fluxcapacitor.FluxValues
import brs.http.common.ResultFields.UNCONFIRMED_TRANSACTIONS_RESPONSE
import brs.peer.Peer
import brs.props.Props
import brs.taskScheduler.Task
import brs.util.JSON
import brs.util.Listeners
import brs.util.isEmpty
import brs.util.logging.safeDebug
import brs.util.logging.safeError
import brs.util.logging.safeInfo
import brs.util.logging.safeTrace
import brs.util.toJsonString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class TransactionProcessorImpl(private val dp: DependencyProvider) : TransactionProcessor {
    private val testUnconfirmedTransactions = dp.propertyService.get(Props.BRS_TEST_UNCONFIRMED_TRANSACTIONS)

    private val transactionListeners = Listeners<Collection<Transaction>, TransactionProcessor.Event>()
    private val foodDispenser: suspend (Peer) -> Collection<Transaction> = { dp.unconfirmedTransactionStore.getAllFor(it) }
    private val doneFeedingLog: suspend (Peer, Collection<Transaction>) -> Unit = { peer, transactions -> dp.unconfirmedTransactionStore.markFingerPrintsOf(peer, transactions) }

    init {
        dp.taskScheduler.scheduleTaskWithDelay(0, 10000) {
            run {
                try {
                    val peer = dp.peers.getAnyPeer(Peer.State.CONNECTED) ?: return@run
                    val response = dp.peers.readUnconfirmedTransactions(peer) ?: return@run
                    val transactionsData = JSON.getAsJsonArray(response.get(UNCONFIRMED_TRANSACTIONS_RESPONSE))
                    if (transactionsData.isEmpty()) return@run
                    dp.peers.feedingTime(peer, foodDispenser, doneFeedingLog)

                    try {
                        val addedTransactions = processPeerTransactions(transactionsData, peer)

                        if (addedTransactions.isNotEmpty()) {
                            val activePrioPlusExtra = dp.peers.allActivePriorityPlusSomeExtraPeers
                            activePrioPlusExtra.remove(peer)

                            val jobs = mutableListOf<Job>()

                            for (otherPeer in activePrioPlusExtra) {
                                jobs.add(launch {
                                    try {
                                        val otherPeerResponse =
                                            dp.peers.readUnconfirmedTransactions(otherPeer) ?: return@launch
                                        val otherPeerTransactions =
                                            JSON.getAsJsonArray(otherPeerResponse.get(UNCONFIRMED_TRANSACTIONS_RESPONSE))
                                        if (otherPeerTransactions.isEmpty()) return@launch
                                        dp.peers.feedingTime(otherPeer, foodDispenser, doneFeedingLog)
                                    } catch (e: ValidationException) {
                                        peer.blacklist(e, "pulled invalid data using getUnconfirmedTransactions")
                                    } catch (e: RuntimeException) {
                                        peer.blacklist(e, "pulled invalid data using getUnconfirmedTransactions")
                                    }
                                })
                            }

                            jobs.forEach { it.join() }
                        }
                    } catch (e: ValidationException) {
                        peer.blacklist(e, "pulled invalid data using getUnconfirmedTransactions")
                    } catch (e: RuntimeException) {
                        peer.blacklist(e, "pulled invalid data using getUnconfirmedTransactions")
                    }
                } catch (e: Exception) {
                    logger.safeDebug(e) { "Error processing unconfirmed transactions" }
                }
            }
        }
    }

    override val allUnconfirmedTransactions: List<Transaction>
        get() = dp.unconfirmedTransactionStore.all

    override val amountUnconfirmedTransactions: Int
        get() = dp.unconfirmedTransactionStore.amount

    override suspend fun addListener(eventType: TransactionProcessor.Event, listener: suspend (Collection<Transaction>) -> Unit) {
        return transactionListeners.addListener(eventType, listener)
    }

    override suspend fun notifyListeners(transactions: Collection<Transaction>, eventType: TransactionProcessor.Event) {
        transactionListeners.accept(eventType, transactions)
    }

    override suspend fun getAllUnconfirmedTransactionsFor(peer: Peer): Collection<Transaction> {
        return dp.unconfirmedTransactionStore.getAllFor(peer)
    }

    override suspend fun markFingerPrintsOf(peer: Peer, transactions: Collection<Transaction>) {
        dp.unconfirmedTransactionStore.markFingerPrintsOf(peer, transactions)
    }

    override suspend fun getUnconfirmedTransaction(transactionId: Long): Transaction? {
        return dp.unconfirmedTransactionStore.get(transactionId)
    }

    override fun newTransactionBuilder(senderPublicKey: ByteArray, amountNQT: Long, feeNQT: Long, deadline: Short, attachment: Attachment): Transaction.Builder {
        val version = getTransactionVersion(dp.blockchain.height).toByte()
        val timestamp = dp.timeService.epochTime
        val builder = Transaction.Builder(dp, version, senderPublicKey, amountNQT, feeNQT, timestamp, deadline, attachment as Attachment.AbstractAttachment)
        if (version > 0) {
            val ecBlock = dp.economicClustering.getECBlock(timestamp)
            builder.ecBlockHeight(ecBlock.height)
            builder.ecBlockId(ecBlock.id)
        }
        return builder
    }

    override suspend fun broadcast(transaction: Transaction): Int? {
        if (!transaction.verifySignature()) {
            throw BurstException.NotValidException("Transaction signature verification failed")
        }
        val processedTransactions = processTransactions(setOf(transaction), null)
        if (dp.transactionDb.hasTransaction(transaction.id)) {
            logger.safeInfo { "Transaction ${transaction.stringId} already in blockchain, will not broadcast again" }
            return null
        }

        if (dp.unconfirmedTransactionStore.exists(transaction.id)) {
            logger.safeInfo { "Transaction ${transaction.stringId} already in unconfirmed pool, will not broadcast again" }
            return null
        }

        if (processedTransactions.isNotEmpty()) {
            return broadcastToPeers(true)
        } else {
            logger.safeDebug { "Could not accept new transaction ${transaction.stringId}" }
            throw BurstException.NotValidException("Invalid transaction " + transaction.stringId)
        }
    }

    override suspend fun processPeerTransactions(request: JsonObject, peer: Peer) {
        val transactionsData = JSON.getAsJsonArray(request.get("transactions"))
        val processedTransactions = processPeerTransactions(transactionsData, peer)

        if (!processedTransactions.isEmpty()) {
            broadcastToPeers(false)
        }
    }

    override fun parseTransaction(bytes: ByteArray): Transaction {
        return Transaction.parseTransaction(dp, bytes)
    }

    override fun parseTransaction(transactionData: JsonObject): Transaction {
        return Transaction.parseTransaction(dp, transactionData, dp.blockchain.height)
    }

    override suspend fun clearUnconfirmedTransactions() {
        val removed: List<Transaction>
        try {
            dp.db.beginTransaction()
            removed = dp.unconfirmedTransactionStore.all
            dp.accountService.flushAccountTable()
            dp.unconfirmedTransactionStore.clear()
            dp.db.commitTransaction()
        } catch (e: Exception) {
            logger.safeError(e) { e.toString() }
            dp.db.rollbackTransaction()
            throw e
        } finally {
            dp.db.endTransaction()
        }

        transactionListeners.accept(TransactionProcessor.Event.REMOVED_UNCONFIRMED_TRANSACTIONS, removed)
    }

    override suspend fun requeueAllUnconfirmedTransactions() {
        dp.unconfirmedTransactionStore.resetAccountBalances()
    }

    override fun getTransactionVersion(blockHeight: Int): Int {
        return if (dp.fluxCapacitor.getValue(FluxValues.DIGITAL_GOODS_STORE, blockHeight)) 1 else 0
    }

    // Watch: This is not really clean
    override suspend fun processLater(transactions: Collection<Transaction>) {
        for (transaction in transactions) {
            try {
                dp.unconfirmedTransactionStore.put(transaction, null)
            } catch (e: ValidationException) {
                logger.safeDebug(e) { "Discarding invalid transaction in for later processing: " + transaction.jsonObject.toJsonString() }
            }
        }
    }

    private suspend fun processPeerTransactions(transactionsData: JsonArray, peer: Peer): Collection<Transaction> {
        if (dp.blockchain.lastBlock.timestamp < dp.timeService.epochTime - 60 * 1440 && !testUnconfirmedTransactions) {
            return mutableListOf()
        }
        val transactions = mutableListOf<Transaction>()
        for (transactionData in transactionsData) {
            try {
                val transaction = parseTransaction(JSON.getAsJsonObject(transactionData))
                dp.transactionService.validate(transaction)
                if (!dp.economicClustering.verifyFork(transaction)) {
                    continue
                }
                transactions.add(transaction)
            } catch (ignored: BurstException.NotCurrentlyValidException) {
            } catch (e: BurstException.NotValidException) {
                logger.safeDebug { "Invalid transaction from peer: ${transactionData.toJsonString()}" }
                throw e
            }

        }
        return processTransactions(transactions, peer)
    }

    private suspend fun processTransactions(transactions: Collection<Transaction>, peer: Peer?): Collection<Transaction> {
        if (transactions.isEmpty()) {
            return emptyList()
        }

        val addedUnconfirmedTransactions = mutableListOf<Transaction>()

        for (transaction in transactions) {

            try {
                val curTime = dp.timeService.epochTime
                if (transaction.timestamp > curTime + 15 || transaction.expiration < curTime
                    || transaction.deadline > 1440) {
                    continue
                }

                dp.db.beginTransaction()
                try {
                    if (dp.transactionDb.hasTransaction(transaction.id) || dp.unconfirmedTransactionStore.exists(transaction.id)) {
                        dp.unconfirmedTransactionStore.markFingerPrintsOf(peer, listOf(transaction))
                    } else if (!(transaction.verifySignature() && dp.transactionService.verifyPublicKey(transaction))) {
                        if (dp.accountService.getAccount(transaction.senderId) != null) {
                            logger.safeDebug { "Transaction ${transaction.jsonObject.toJsonString()} failed to verify" }
                        }
                    } else if (dp.unconfirmedTransactionStore.put(transaction, peer)) {
                        addedUnconfirmedTransactions.add(transaction)
                    }
                    dp.db.commitTransaction()
                } catch (e: Exception) {
                    dp.db.rollbackTransaction()
                    throw e
                } finally {
                    dp.db.endTransaction()
                }
            } catch (e: RuntimeException) {
                logger.safeInfo(e) { "Error processing transaction" }
            }

        }

        if (addedUnconfirmedTransactions.isNotEmpty()) {
            transactionListeners.accept(
                TransactionProcessor.Event.ADDED_UNCONFIRMED_TRANSACTIONS,
                addedUnconfirmedTransactions
            )
        }

        return addedUnconfirmedTransactions
    }

    private suspend fun broadcastToPeers(toAll: Boolean): Int {
        val peersToSendTo = if (toAll) dp.peers.activePeers.take(100) else dp.peers.allActivePriorityPlusSomeExtraPeers

        logger.safeTrace { "Queueing up ${peersToSendTo.size} Peers for feeding" }

        for (p in peersToSendTo) {
            dp.peers.feedingTime(p, foodDispenser, doneFeedingLog)
        }

        return peersToSendTo.size
    }

    override suspend fun revalidateUnconfirmedTransactions() {
        val invalidTransactions = mutableListOf<Transaction>()

        for (t in dp.unconfirmedTransactionStore.all) {
            try {
                dp.transactionService.validate(t)
            } catch (e: ValidationException) {
                invalidTransactions.add(t)
            }

        }

        for (t in invalidTransactions) {
            dp.unconfirmedTransactionStore.remove(t)
        }
    }

    override suspend fun removeForgedTransactions(transactions: Collection<Transaction>) {
        dp.unconfirmedTransactionStore.removeForgedTransactions(transactions)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TransactionProcessorImpl::class.java)
    }
}
