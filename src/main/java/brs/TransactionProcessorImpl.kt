package brs

import brs.BurstException.ValidationException
import brs.db.sql.Db
import brs.fluxcapacitor.FluxValues
import brs.http.common.ResultFields.UNCONFIRMED_TRANSACTIONS_RESPONSE
import brs.peer.Peer
import brs.props.Props
import brs.util.JSON
import brs.util.Listeners
import brs.util.isEmpty
import brs.util.toJsonString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import kotlin.system.exitProcess

class TransactionProcessorImpl(private val dp: DependencyProvider) : TransactionProcessor {

    private val testUnconfirmedTransactions = dp.propertyService.get(Props.BRS_TEST_UNCONFIRMED_TRANSACTIONS)

    private val unconfirmedTransactionsSyncObj = Any()
    private val transactionListeners = Listeners<Collection<Transaction>, TransactionProcessor.Event>()
    private val foodDispenser: (Peer) -> Collection<Transaction> = { dp.unconfirmedTransactionStore.getAllFor(it) }
    private val doneFeedingLog: (Peer, Collection<Transaction>) -> Unit = { peer, transactions -> dp.unconfirmedTransactionStore.markFingerPrintsOf(peer, transactions) }

    override val allUnconfirmedTransactions: List<Transaction>
        get() = dp.unconfirmedTransactionStore.all

    override val amountUnconfirmedTransactions: Int
        get() = dp.unconfirmedTransactionStore.amount

    init {
        val getUnconfirmedTransactions: () -> Unit = {
            try {
                try {
                    synchronized(unconfirmedTransactionsSyncObj) {
                        val peer = dp.peers.getAnyPeer(Peer.State.CONNECTED) ?: return@synchronized
                        val response = dp.peers.readUnconfirmedTransactionsNonBlocking(peer).get() ?: return@synchronized
                        val transactionsData = JSON.getAsJsonArray(response!!.get(UNCONFIRMED_TRANSACTIONS_RESPONSE))
                        if (transactionsData.isEmpty()) return@synchronized

                        try {
                            val addedTransactions = processPeerTransactions(transactionsData, peer)
                            dp.peers.feedingTime(peer, foodDispenser, doneFeedingLog)

                            if (addedTransactions.isNotEmpty()) {
                                val activePrioPlusExtra = dp.peers.allActivePriorityPlusSomeExtraPeers
                                activePrioPlusExtra.remove(peer)

                                val expectedResults = mutableListOf<CompletableFuture<*>>()

                                for (otherPeer in activePrioPlusExtra) {
                                    val unconfirmedTransactionsResult = dp.peers.readUnconfirmedTransactionsNonBlocking(otherPeer)

                                    unconfirmedTransactionsResult.whenComplete { jsonObject, throwable ->
                                        try {
                                            processPeerTransactions(transactionsData, otherPeer)
                                            dp.peers.feedingTime(otherPeer, foodDispenser, doneFeedingLog)
                                        } catch (e: ValidationException) {
                                            peer!!.blacklist(e, "pulled invalid data using getUnconfirmedTransactions")
                                        } catch (e: RuntimeException) {
                                            peer!!.blacklist(e, "pulled invalid data using getUnconfirmedTransactions")
                                        }
                                    }

                                    expectedResults.add(unconfirmedTransactionsResult)
                                }

                                CompletableFuture.allOf(*expectedResults.toTypedArray()).join()
                            }
                        } catch (e: ValidationException) {
                            peer!!.blacklist(e, "pulled invalid data using getUnconfirmedTransactions")
                        } catch (e: RuntimeException) {
                            peer!!.blacklist(e, "pulled invalid data using getUnconfirmedTransactions")
                        }

                    }
                } catch (e: Exception) {
                    logger.debug("Error processing unconfirmed transactions", e)
                }

            } catch (t: Exception) {
                logger.info("CRITICAL ERROR. PLEASE REPORT TO THE DEVELOPERS.\n$t", t)
                exitProcess(1)
            }
        }
        dp.threadPool.scheduleThread("PullUnconfirmedTransactions", getUnconfirmedTransactions, 5)
    }

    override fun addListener(listener: (Collection<Transaction>) -> Unit, eventType: TransactionProcessor.Event): Boolean {
        return transactionListeners.addListener(listener, eventType)
    }

    override fun removeListener(listener: (Collection<Transaction>) -> Unit, eventType: TransactionProcessor.Event): Boolean {
        return transactionListeners.removeListener(listener, eventType)
    }

    override fun notifyListeners(transactions: Collection<Transaction>, eventType: TransactionProcessor.Event) {
        transactionListeners.accept(transactions, eventType)
    }

    override fun getUnconfirmedTransactionsSyncObj(): Any {
        return unconfirmedTransactionsSyncObj
    }

    override fun getAllUnconfirmedTransactionsFor(peer: Peer): Collection<Transaction> {
        return dp.unconfirmedTransactionStore.getAllFor(peer)
    }

    override fun markFingerPrintsOf(peer: Peer, transactions: Collection<Transaction>) {
        dp.unconfirmedTransactionStore.markFingerPrintsOf(peer, transactions)
    }

    override fun getUnconfirmedTransaction(transactionId: Long): Transaction? {
        return dp.unconfirmedTransactionStore[transactionId]
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

    override fun broadcast(transaction: Transaction): Int? {
        if (!transaction.verifySignature()) {
            throw BurstException.NotValidException("Transaction signature verification failed")
        }
        val processedTransactions = processTransactions(setOf(transaction), null)
        if (dp.transactionDb.hasTransaction(transaction.id)) {
            if (logger.isInfoEnabled) {
                logger.info("Transaction {} already in blockchain, will not broadcast again", transaction.stringId)
            }
            return null
        }

        if (dp.unconfirmedTransactionStore.exists(transaction.id)) {
            if (logger.isInfoEnabled) {
                logger.info("Transaction {} already in unconfirmed pool, will not broadcast again", transaction.stringId)
            }
            return null
        }

        if (processedTransactions.isNotEmpty()) {
            return broadcastToPeers(true)
        } else {
            if (logger.isDebugEnabled) {
                logger.debug("Could not accept new transaction {}", transaction.stringId)
            }
            throw BurstException.NotValidException("Invalid transaction " + transaction.stringId)
        }
    }

    override fun processPeerTransactions(request: JsonObject, peer: Peer) {
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

    override fun clearUnconfirmedTransactions() {
        synchronized(unconfirmedTransactionsSyncObj) {
            val removed: List<Transaction>
            try {
                Db.beginTransaction()
                removed = dp.unconfirmedTransactionStore.all
                dp.accountService.flushAccountTable()
                dp.unconfirmedTransactionStore.clear()
                Db.commitTransaction()
            } catch (e: Exception) {
                logger.error(e.toString(), e)
                Db.rollbackTransaction()
                throw e
            } finally {
                Db.endTransaction()
            }

            transactionListeners.accept(removed, TransactionProcessor.Event.REMOVED_UNCONFIRMED_TRANSACTIONS)
        }
    }

    override fun requeueAllUnconfirmedTransactions() {
        synchronized(unconfirmedTransactionsSyncObj) {
            dp.unconfirmedTransactionStore.resetAccountBalances()
        }
    }

    override fun getTransactionVersion(previousBlockHeight: Int): Int {
        return if (dp.fluxCapacitor.getValue(FluxValues.DIGITAL_GOODS_STORE, previousBlockHeight)) 1 else 0
    }

    // Watch: This is not really clean
    override fun processLater(transactions: Collection<Transaction>) {
        for (transaction in transactions) {
            try {
                dp.unconfirmedTransactionStore.put(transaction, null)
            } catch (e: BurstException.ValidationException) {
                logger.debug("Discarding invalid transaction in for later processing: " + transaction.jsonObject.toJsonString(), e)
            }

        }
    }

    private fun processPeerTransactions(transactionsData: JsonArray, peer: Peer): Collection<Transaction> {
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
            } catch (ignore: BurstException.NotCurrentlyValidException) {
            } catch (e: BurstException.NotValidException) {
                if (logger.isDebugEnabled) {
                    logger.debug("Invalid transaction from peer: {}", transactionData.toJsonString())
                }
                throw e
            }

        }
        return processTransactions(transactions, peer)
    }

    private fun processTransactions(transactions: Collection<Transaction>, peer: Peer?): Collection<Transaction> {
        synchronized(unconfirmedTransactionsSyncObj) {
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

                    Db.beginTransaction()
                    try {
                        if (dp.transactionDb.hasTransaction(transaction.id) || dp.unconfirmedTransactionStore.exists(transaction.id)) {
                            dp.unconfirmedTransactionStore.markFingerPrintsOf(peer, listOf(transaction))
                        } else if (!(transaction.verifySignature() && dp.transactionService.verifyPublicKey(transaction))) {
                            if (dp.accountService.getAccount(transaction.senderId) != null && logger.isDebugEnabled) {
                                logger.debug("Transaction {} failed to verify", transaction.jsonObject.toJsonString())
                            }
                        } else if (dp.unconfirmedTransactionStore.put(transaction, peer)) {
                            addedUnconfirmedTransactions.add(transaction)
                        }
                        Db.commitTransaction()
                    } catch (e: Exception) {
                        Db.rollbackTransaction()
                        throw e
                    } finally {
                        Db.endTransaction()
                    }
                } catch (e: RuntimeException) {
                    logger.info("Error processing transaction", e)
                }

            }

            if (addedUnconfirmedTransactions.isNotEmpty()) {
                transactionListeners.accept(addedUnconfirmedTransactions, TransactionProcessor.Event.ADDED_UNCONFIRMED_TRANSACTIONS)
            }

            return addedUnconfirmedTransactions
        }
    }

    private fun broadcastToPeers(toAll: Boolean): Int {
        val peersToSendTo = if (toAll) dp.peers.activePeers.take(100) else dp.peers.allActivePriorityPlusSomeExtraPeers

        logger.trace("Queueing up {} Peers for feeding", peersToSendTo.size)

        for (p in peersToSendTo) {
            dp.peers.feedingTime(p, foodDispenser, doneFeedingLog)
        }

        return peersToSendTo.size
    }

    override fun revalidateUnconfirmedTransactions() {
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

    override fun removeForgedTransactions(transactions: Collection<Transaction>) {
        dp.unconfirmedTransactionStore.removeForgedTransactions(transactions)
    }

    companion object {

        private val logger = LoggerFactory.getLogger(TransactionProcessorImpl::class.java)
    }
}
