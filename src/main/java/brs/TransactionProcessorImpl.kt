package brs

import brs.BurstException.ValidationException
import brs.db.sql.Db
import brs.db.store.Dbs
import brs.fluxcapacitor.FluxValues
import brs.peer.Peer
import brs.peer.Peers
import brs.props.PropertyService
import brs.props.Props
import brs.services.AccountService
import brs.services.TimeService
import brs.services.TransactionService
import brs.unconfirmedtransactions.UnconfirmedTransactionStore
import brs.util.JSON
import brs.util.Listeners
import brs.util.ThreadPool
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.ArrayList
import java.util.Collections
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors

import brs.http.common.ResultFields.UNCONFIRMED_TRANSACTIONS_RESPONSE

class TransactionProcessorImpl(private val dp: DependencyProvider) : TransactionProcessor {

    private val testUnconfirmedTransactions: Boolean

    private val unconfirmedTransactionsSyncObj = Any()
    private val transactionListeners = Listeners<List<Transaction>, TransactionProcessor.Event>()
    private val foodDispenser: Function<Peer, List<Transaction>>
    private val doneFeedingLog: BiConsumer<Peer, List<Transaction>>

    override val allUnconfirmedTransactions: List<Transaction>
        get() = dp.unconfirmedTransactionStore.all

    override val amountUnconfirmedTransactions: Int
        get() = dp.unconfirmedTransactionStore.amount

    init {

        this.testUnconfirmedTransactions = dp.propertyService.get(Props.BRS_TEST_UNCONFIRMED_TRANSACTIONS)

        this.foodDispenser = Function { dp.unconfirmedTransactionStore.getAllFor(it) }
        this.doneFeedingLog = BiConsumer { peer, transactions -> dp.unconfirmedTransactionStore.markFingerPrintsOf(peer, transactions) }

        val getUnconfirmedTransactions = {
            try {
                try {
                    synchronized(unconfirmedTransactionsSyncObj) {
                        val peer = Peers.getAnyPeer(Peer.State.CONNECTED)
                        if (peer == null) {
                            return
                        }
                        val response = Peers.readUnconfirmedTransactionsNonBlocking(peer).get()
                        if (response == null) {
                            return
                        }

                        val transactionsData = JSON.getAsJsonArray(response!!.get(UNCONFIRMED_TRANSACTIONS_RESPONSE))

                        if (transactionsData == null || transactionsData.size() == 0) {
                            return
                        }

                        try {
                            val addedTransactions = processPeerTransactions(transactionsData, peer)
                            Peers.feedingTime(peer, foodDispenser, doneFeedingLog)

                            if (!addedTransactions.isEmpty()) {
                                val activePrioPlusExtra = Peers.allActivePriorityPlusSomeExtraPeers
                                activePrioPlusExtra.remove(peer)

                                val expectedResults = mutableListOf<CompletableFuture<*>>()

                                for (otherPeer in activePrioPlusExtra) {
                                    val unconfirmedTransactionsResult = Peers.readUnconfirmedTransactionsNonBlocking(otherPeer)

                                    unconfirmedTransactionsResult.whenComplete { jsonObject, throwable ->
                                        try {
                                            processPeerTransactions(transactionsData, otherPeer)
                                            Peers.feedingTime(otherPeer, foodDispenser, doneFeedingLog)
                                        } catch (e: ValidationException) {
                                            peer!!.blacklist(e, "pulled invalid data using getUnconfirmedTransactions")
                                        } catch (e: RuntimeException) {
                                            peer!!.blacklist(e, "pulled invalid data using getUnconfirmedTransactions")
                                        }
                                    }

                                    expectedResults.add(unconfirmedTransactionsResult)
                                }

                                CompletableFuture.allOf(*expectedResults.toTypedArray<CompletableFuture>()).join()
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
                System.exit(1)
            }
        }
        dp.threadPool.scheduleThread("PullUnconfirmedTransactions", getUnconfirmedTransactions, 5)
    }

    override fun addListener(listener: Consumer<List<Transaction>>, eventType: TransactionProcessor.Event): Boolean {
        return transactionListeners.addListener(listener, eventType)
    }

    override fun removeListener(listener: Consumer<List<Transaction>>, eventType: TransactionProcessor.Event): Boolean {
        return transactionListeners.removeListener(listener, eventType)
    }

    override fun notifyListeners(transactions: List<Transaction>, eventType: TransactionProcessor.Event) {
        transactionListeners.accept(transactions, eventType)
    }

    override fun getUnconfirmedTransactionsSyncObj(): Any {
        return unconfirmedTransactionsSyncObj
    }

    override fun getAllUnconfirmedTransactionsFor(peer: Peer): List<Transaction> {
        return dp.unconfirmedTransactionStore.getAllFor(peer)
    }

    override fun markFingerPrintsOf(peer: Peer, transactions: List<Transaction>) {
        dp.unconfirmedTransactionStore.markFingerPrintsOf(peer, transactions)
    }

    override fun getUnconfirmedTransaction(transactionId: Long): Transaction {
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

    @Throws(BurstException.ValidationException::class)
    override fun broadcast(transaction: Transaction): Int? {
        if (!transaction.verifySignature()) {
            throw BurstException.NotValidException("Transaction signature verification failed")
        }
        val processedTransactions: List<Transaction>
        if (dp.dbs.transactionDb.hasTransaction(transaction.id)) {
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

        processedTransactions = processTransactions(setOf(transaction), null)

        if (!processedTransactions.isEmpty()) {
            return broadcastToPeers(true)
        } else {
            if (logger.isDebugEnabled) {
                logger.debug("Could not accept new transaction {}", transaction.stringId)
            }
            throw BurstException.NotValidException("Invalid transaction " + transaction.stringId)
        }
    }

    @Throws(BurstException.ValidationException::class)
    override fun processPeerTransactions(request: JsonObject, peer: Peer) {
        val transactionsData = JSON.getAsJsonArray(request.get("transactions"))
        val processedTransactions = processPeerTransactions(transactionsData, peer)

        if (!processedTransactions.isEmpty()) {
            broadcastToPeers(false)
        }
    }

    @Throws(BurstException.ValidationException::class)
    override fun parseTransaction(bytes: ByteArray): Transaction {
        return Transaction.parseTransaction(dp, bytes)
    }

    @Throws(BurstException.NotValidException::class)
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
                logger.debug("Discarding invalid transaction in for later processing: " + JSON.toJsonString(transaction.jsonObject), e)
            }

        }
    }

    @Throws(BurstException.ValidationException::class)
    private fun processPeerTransactions(transactionsData: JsonArray, peer: Peer): List<Transaction> {
        if (dp.blockchain.lastBlock.timestamp < dp.timeService.epochTime - 60 * 1440 && !testUnconfirmedTransactions) {
            return mutableListOf()
        }
        if (dp.blockchain.height <= Constants.NQT_BLOCK) {
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
                    logger.debug("Invalid transaction from peer: {}", JSON.toJsonString(transactionData))
                }
                throw e
            }

        }
        return processTransactions(transactions, peer)
    }

    @Throws(BurstException.ValidationException::class)
    private fun processTransactions(transactions: Collection<Transaction>, peer: Peer?): List<Transaction> {
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
                    if (dp.blockchain.height < Constants.NQT_BLOCK) {
                        break // not ready to process transactions
                    }
                    try {
                        if (dp.dbs.transactionDb.hasTransaction(transaction.id) || dp.unconfirmedTransactionStore.exists(transaction.id)) {
                            dp.unconfirmedTransactionStore.markFingerPrintsOf(peer, listOf(transaction))
                        } else if (!(transaction.verifySignature() && dp.transactionService.verifyPublicKey(transaction))) {
                            if (dp.accountService.getAccount(transaction.senderId) != null && logger.isDebugEnabled) {
                                logger.debug("Transaction {} failed to verify", JSON.toJsonString(transaction.jsonObject))
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

            if (!addedUnconfirmedTransactions.isEmpty()) {
                transactionListeners.accept(addedUnconfirmedTransactions, TransactionProcessor.Event.ADDED_UNCONFIRMED_TRANSACTIONS)
            }

            return addedUnconfirmedTransactions
        }
    }

    private fun broadcastToPeers(toAll: Boolean): Int {
        val peersToSendTo = if (toAll) Peers.activePeers.take(100) else Peers.allActivePriorityPlusSomeExtraPeers

        logger.trace("Queueing up {} Peers for feeding", peersToSendTo.size)

        for (p in peersToSendTo) {
            Peers.feedingTime(p, foodDispenser, doneFeedingLog)
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

    override fun removeForgedTransactions(transactions: MutableList<Transaction>) {
        dp.unconfirmedTransactionStore.removeForgedTransactions(transactions)
    }

    companion object {

        private val logger = LoggerFactory.getLogger(TransactionProcessorImpl::class.java)
    }
}
