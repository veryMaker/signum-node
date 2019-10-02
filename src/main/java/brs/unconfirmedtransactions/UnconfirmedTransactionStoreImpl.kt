package brs.unconfirmedtransactions

import brs.Constants
import brs.DependencyProvider
import brs.Transaction
import brs.peer.Peer
import brs.props.Props
import brs.taskScheduler.Task
import brs.transactionduplicates.TransactionDuplicatesCheckerImpl
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.util.*

class UnconfirmedTransactionStoreImpl private constructor(private val dp: DependencyProvider) : UnconfirmedTransactionStore {
    private val reservedBalanceCache = ReservedBalanceCache(dp.accountStore)
    private val transactionDuplicatesChecker = TransactionDuplicatesCheckerImpl()

    private val fingerPrintsOverview = mutableMapOf<Transaction, HashSet<Peer?>>()

    private val internalStore: SortedMap<Long, MutableList<Transaction>> = TreeMap()
    private val internalStoreLock = Mutex()

    override var amount: Int = 0
    private val maxSize = dp.propertyService.get(Props.P2P_MAX_UNCONFIRMED_TRANSACTIONS)

    private val maxRawUTBytesToSend = dp.propertyService.get(Props.P2P_MAX_UNCONFIRMED_TRANSACTIONS_RAW_SIZE_BYTES_TO_SEND)

    private val maxPercentageUnconfirmedTransactionsFullHash = dp.propertyService.get(Props.P2P_MAX_PERCENTAGE_UNCONFIRMED_TRANSACTIONS_FULL_HASH_REFERENCE)
    private var numberUnconfirmedTransactionsFullHash: Int = 0

    val cleanupExpiredTransactions: Task = {
        internalStoreLock.withLock {
            all.filter { t -> dp.timeService.epochTime > t.expiration || dp.transactionDb.hasTransaction(t.id) }
                .forEach { removeTransaction(it) }
        }
    }

    override val all: List<Transaction>
        get() = runBlocking {
            internalStoreLock.withLock {
                val flatTransactionList = mutableListOf<Transaction>()

                for (amountSlot in internalStore.values) {
                    flatTransactionList.addAll(amountSlot)
                }

                flatTransactionList
            }
        }

    override suspend fun put(transaction: Transaction, peer: Peer?): Boolean {
        internalStoreLock.withLock {
            if (transactionIsCurrentlyInCache(transaction)) {
                if (peer != null) {
                    logger.info("Transaction {}: Added fingerprint of {}", transaction.id, peer.peerAddress)
                    fingerPrintsOverview[transaction]!!.add(peer)
                }
            } else if (transactionCanBeAddedToCache(transaction)) {
                this.reservedBalanceCache.reserveBalanceAndPut(transaction)

                val duplicationInformation = transactionDuplicatesChecker.removeCheaperDuplicate(transaction)

                if (duplicationInformation.isDuplicate) {
                    val duplicatedTransaction = duplicationInformation.transaction

                    if (duplicatedTransaction != null && duplicatedTransaction !== transaction) {
                        logger.info("Transaction {}: Adding more expensive duplicate transaction", transaction.id)
                        removeTransaction(duplicationInformation.transaction)
                        this.reservedBalanceCache.refundBalance(duplicationInformation.transaction)

                        addTransaction(transaction, peer)

                        if (amount > maxSize) {
                            removeCheapestFirstToExpireTransaction()
                        }
                    } else {
                        logger.info("Transaction {}: Will not add a cheaper duplicate UT", transaction.id)
                    }
                } else {
                    addTransaction(transaction, peer)
                    if (amount % 128 == 0) {
                        logger.info("Cache size: {}/{} added {} from sender {}", amount, maxSize, transaction.id, transaction.senderId)
                    } else {
                        logger.debug("Cache size: {}/{} added {} from sender {}", amount, maxSize, transaction.id, transaction.senderId)
                    }
                }

                if (amount > maxSize) {
                    removeCheapestFirstToExpireTransaction()
                }

                return true
            }

            return false
        }
    }

    override suspend fun get(transactionId: Long?): Transaction? {
        internalStoreLock.withLock {
            for (amountSlot in internalStore.values) {
                for (t in amountSlot) {
                    if (t.id == transactionId) {
                        return t
                    }
                }
            }
            return null
        }
    }

    override suspend fun exists(transactionId: Long?): Boolean {
        internalStoreLock.withLock {
            return get(transactionId) != null
        }
    }

    override suspend fun getAllFor(peer: Peer): List<Transaction> {
        internalStoreLock.withLock {
            var roomLeft = this.maxRawUTBytesToSend.toLong()
            return fingerPrintsOverview.entries
                    .asSequence()
                    .filter { e -> !e.value.contains(peer) }
                    .map { it.key }
                    .toList()
                    .filter {
                        roomLeft -= it.size.toLong()
                        return@filter roomLeft > 0
                    }
                    .toList()
        }
    }

    override suspend fun remove(transaction: Transaction) {
        internalStoreLock.withLock {
            // Make sure that we are acting on our own copy of the transaction, as this is the one we want to remove.
            val internalTransaction = get(transaction.id)
            if (internalTransaction != null) {
                logger.debug("Removing {}", transaction.id)
                removeTransaction(internalTransaction)
            }
        }
    }

    override suspend fun clear() {
        internalStoreLock.withLock {
            logger.info("Clearing UTStore")
            amount = 0
            internalStore.clear()
            reservedBalanceCache.clear()
            transactionDuplicatesChecker.clear()
        }
    }

    override suspend fun resetAccountBalances() {
        internalStoreLock.withLock {
            for (insufficientFundsTransactions in reservedBalanceCache.rebuild(all)) {
                this.removeTransaction(insufficientFundsTransactions)
            }
        }
    }

    override suspend fun markFingerPrintsOf(peer: Peer?, transactions: Collection<Transaction>) {
        internalStoreLock.withLock {
            for (transaction in transactions) {
                if (fingerPrintsOverview.containsKey(transaction)) {
                    fingerPrintsOverview[transaction]!!.add(peer)
                }
            }
        }
    }

    override suspend fun removeForgedTransactions(transactions: Collection<Transaction>) {
        internalStoreLock.withLock {
            for (t in transactions) {
                remove(t)
            }
        }
    }

    private fun transactionIsCurrentlyInCache(transaction: Transaction): Boolean {
        val amountSlot = internalStore[amountSlotForTransaction(transaction)]
        return amountSlot != null && amountSlot.any { t -> t.id == transaction.id }
    }

    private fun transactionCanBeAddedToCache(transaction: Transaction): Boolean {
        return (transactionIsCurrentlyNotExpired(transaction)
                && !cacheFullAndTransactionCheaperThanAllTheRest(transaction)
                && !tooManyTransactionsWithReferencedFullHash(transaction)
                && !tooManyTransactionsForSlotSize(transaction))
    }

    private fun tooManyTransactionsForSlotSize(transaction: Transaction): Boolean {
        val slotHeight = this.amountSlotForTransaction(transaction)

        if (this.internalStore.containsKey(slotHeight) && this.internalStore[slotHeight]!!.size.toLong() == slotHeight * 360) {
            logger.info("Transaction {}: Not added because slot {} is full", transaction.id, slotHeight)
            return true
        }

        return false
    }

    private fun tooManyTransactionsWithReferencedFullHash(transaction: Transaction): Boolean {
        if (transaction.referencedTransactionFullHash != null && maxPercentageUnconfirmedTransactionsFullHash <= (numberUnconfirmedTransactionsFullHash + 1) * 100 / maxSize) {
            logger.info("Transaction {}: Not added because too many transactions with referenced full hash", transaction.id)
            return true
        }

        return false
    }

    private fun cacheFullAndTransactionCheaperThanAllTheRest(transaction: Transaction): Boolean {
        if (amount == maxSize && internalStore.firstKey() > amountSlotForTransaction(transaction)) {
            logger.info("Transaction {}: Not added because cache is full and transaction is cheaper than all the rest", transaction.id)
            return true
        }

        return false
    }

    private fun transactionIsCurrentlyNotExpired(transaction: Transaction): Boolean {
        return if (dp.timeService.epochTime < transaction.expiration) {
            true
        } else {
            logger.info("Transaction {} past expiration: {}", transaction.id, transaction.expiration)
            false
        }
    }

    private fun addTransaction(transaction: Transaction, peer: Peer?) {
        val slot = getOrCreateAmountSlotForTransaction(transaction)
        slot.add(transaction)
        amount++

        fingerPrintsOverview[transaction] = HashSet()

        if (peer != null) {
            fingerPrintsOverview[transaction]!!.add(peer)
        }

        if (logger.isDebugEnabled) {
            if (peer == null) {
                logger.debug("Adding Transaction {} from ourself", transaction.id)
            } else {
                logger.debug("Adding Transaction {} from Peer {}", transaction.id, peer.peerAddress)
            }
        }

        if (transaction.referencedTransactionFullHash != null) {
            numberUnconfirmedTransactionsFullHash++
        }
    }

    private fun getOrCreateAmountSlotForTransaction(transaction: Transaction): MutableList<Transaction> {
        val amountSlotNumber = amountSlotForTransaction(transaction)

        if (!this.internalStore.containsKey(amountSlotNumber)) {
            this.internalStore[amountSlotNumber] = mutableListOf()
        }

        return this.internalStore[amountSlotNumber]!!
    }


    private fun amountSlotForTransaction(transaction: Transaction): Long {
        return transaction.feeNQT / Constants.FEE_QUANT
    }

    private fun removeCheapestFirstToExpireTransaction() {
        val cheapestFirstToExpireTransaction = this.internalStore[this.internalStore.firstKey()]!!
                .sortedWith(Comparator.comparingLong<Transaction> { it.feeNQT }
                        .thenComparing<Int> { it.expiration }
                        .thenComparing<Long> { it.id })
                .firstOrNull()
        if (cheapestFirstToExpireTransaction != null) {
            reservedBalanceCache.refundBalance(cheapestFirstToExpireTransaction)
            removeTransaction(cheapestFirstToExpireTransaction)
        }
    }

    private fun removeTransaction(transaction: Transaction?) {
        if (transaction == null) return
        val amountSlotNumber = amountSlotForTransaction(transaction)

        val amountSlot = internalStore[amountSlotNumber]!!

        fingerPrintsOverview.remove(transaction)
        amountSlot.remove(transaction)
        amount--
        transactionDuplicatesChecker.removeTransaction(transaction)

        if (transaction.referencedTransactionFullHash != null) {
            numberUnconfirmedTransactionsFullHash--
        }

        if (amountSlot.isEmpty()) {
            this.internalStore.remove(amountSlotNumber)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UnconfirmedTransactionStoreImpl::class.java)

        suspend fun new(dp: DependencyProvider): UnconfirmedTransactionStore {
            val utStore = UnconfirmedTransactionStoreImpl(dp)

            dp.taskScheduler.scheduleTaskWithDelay(utStore.cleanupExpiredTransactions, 10000, 10000)

            return utStore
        }
    }
}
