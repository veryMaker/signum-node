package brs.services.impl

import brs.db.AccountStore
import brs.entity.Account
import brs.entity.DependencyProvider
import brs.entity.Transaction
import brs.objects.Constants
import brs.objects.Props
import brs.peer.Peer
import brs.services.TaskType
import brs.services.UnconfirmedTransactionService
import brs.util.BurstException
import brs.util.TransactionDuplicateChecker
import brs.util.convert.safeAdd
import brs.util.convert.safeSubtract
import brs.util.logging.safeDebug
import brs.util.logging.safeInfo
import brs.util.sync.Mutex
import org.slf4j.LoggerFactory
import java.util.*

class UnconfirmedTransactionServiceImpl(private val dp: DependencyProvider) :
    UnconfirmedTransactionService {
    private val reservedBalanceCache =
        ReservedBalanceCache(dp.accountStore)
    private val transactionDuplicatesChecker = TransactionDuplicateChecker()

    private val fingerPrintsOverview = mutableMapOf<Transaction, MutableSet<Peer?>>()

    private val internalStore: SortedMap<Long, MutableList<Transaction>> = TreeMap()
    private val internalStoreLock = Mutex()

    override var amount: Int = 0
    private val maxSize = dp.propertyService.get(Props.P2P_MAX_UNCONFIRMED_TRANSACTIONS)

    private val maxRawUTBytesToSend =
        dp.propertyService.get(Props.P2P_MAX_UNCONFIRMED_TRANSACTIONS_RAW_SIZE_BYTES_TO_SEND)

    private val maxPercentageUnconfirmedTransactionsFullHash =
        dp.propertyService.get(Props.P2P_MAX_PERCENTAGE_UNCONFIRMED_TRANSACTIONS_FULL_HASH_REFERENCE)
    private var numberUnconfirmedTransactionsFullHash: Int = 0

    init {
        dp.taskSchedulerService.scheduleTaskWithDelay(TaskType.IO, 10000, 10000) {
            internalStoreLock.withLock {
                getAllNoLock().filter { t ->
                    dp.timeService.epochTime > t.expiration || dp.transactionDb.hasTransaction(
                        t.id
                    )
                }
                    .forEach { removeTransaction(it) }
            }
        }
    }

    /**
     * Assumes locked.
     */
    private fun getAllNoLock(): List<Transaction> {
        val flatTransactionList = mutableListOf<Transaction>()

        for (amountSlot in internalStore.values) {
            flatTransactionList.addAll(amountSlot)
        }

        return flatTransactionList
    }


    override val all: List<Transaction>
        get() = internalStoreLock.withLock {
            getAllNoLock()
        }

    override fun put(transaction: Transaction, peer: Peer?): Boolean {
        internalStoreLock.withLock {
            if (transactionIsCurrentlyInCache(transaction)) {
                if (peer != null) {
                    logger.safeInfo { "Transaction ${transaction.id}: Added fingerprint of ${peer.peerAddress}" }
                    fingerPrintsOverview[transaction]!!.add(peer)
                }
            } else if (transactionCanBeAddedToCache(transaction)) {
                this.reservedBalanceCache.reserveBalanceAndPut(transaction)

                val duplicationInformation = transactionDuplicatesChecker.removeCheaperDuplicate(transaction)

                if (duplicationInformation.isDuplicate) {
                    val duplicatedTransaction = duplicationInformation.transaction

                    if (duplicatedTransaction != null && duplicatedTransaction !== transaction) {
                        logger.safeInfo { "Transaction ${transaction.id}: Adding more expensive duplicate transaction" }
                        removeTransaction(duplicationInformation.transaction)
                        this.reservedBalanceCache.refundBalance(duplicationInformation.transaction)

                        addTransaction(transaction, peer)

                        if (amount > maxSize) {
                            removeCheapestFirstToExpireTransaction()
                        }
                    } else {
                        logger.safeInfo { "Transaction ${transaction.id}: Will not add a cheaper duplicate UT" }
                    }
                } else {
                    addTransaction(transaction, peer)
                    if (amount % 128 == 0) {
                        logger.safeInfo { "Cache size: $amount/$maxSize added ${transaction.id} from sender ${transaction.senderId}" }
                    } else {
                        logger.safeDebug { "Cache size: $amount/$maxSize added ${transaction.id} from sender ${transaction.senderId}" }
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

    /**
     * Assumes locked.
     */
    private fun getNoLock(transactionId: Long?): Transaction? {
        for (amountSlot in internalStore.values) {
            for (t in amountSlot) {
                if (t.id == transactionId) {
                    return t
                }
            }
        }
        return null
    }

    override fun get(transactionId: Long?): Transaction? {
        internalStoreLock.withLock {
            return getNoLock(transactionId)
        }
    }

    override fun exists(transactionId: Long?): Boolean {
        internalStoreLock.withLock {
            return getNoLock(transactionId) != null
        }
    }

    override fun getAllFor(peer: Peer): List<Transaction> {
        internalStoreLock.withLock {
            var roomLeft = this.maxRawUTBytesToSend.toLong()
            return fingerPrintsOverview.entries
                .asSequence()
                .filter { e -> !e.value.contains(peer) }
                .map { it.key }
                .filter {
                    roomLeft -= it.size.toLong()
                    return@filter roomLeft > 0
                }
                .toList()
        }
    }

    /**
     * Assumes locked.
     */
    private fun removeNoLock(transaction: Transaction) {
        // Make sure that we are acting on our own copy of the transaction, as this is the one we want to remove. TODO check this
        val internalTransaction = getNoLock(transaction.id)
        if (internalTransaction != null) {
            logger.safeDebug { "Removing ${transaction.id}" }
            removeTransaction(internalTransaction)
        }
    }

    override fun remove(transaction: Transaction) {
        internalStoreLock.withLock {
            removeNoLock(transaction)
        }
    }

    override fun clear() {
        internalStoreLock.withLock {
            logger.safeInfo { "Clearing UTStore" }
            amount = 0
            internalStore.clear()
            reservedBalanceCache.clear()
            transactionDuplicatesChecker.clear()
        }
    }

    override fun resetAccountBalances() {
        internalStoreLock.withLock {
            for (insufficientFundsTransactions in reservedBalanceCache.rebuild(getAllNoLock())) {
                removeTransaction(insufficientFundsTransactions)
            }
        }
    }

    override fun markFingerPrintsOf(peer: Peer?, transactions: Collection<Transaction>) {
        internalStoreLock.withLock {
            for (transaction in transactions) {
                if (fingerPrintsOverview.containsKey(transaction)) {
                    fingerPrintsOverview[transaction]!!.add(peer)
                }
            }
        }
    }

    override fun removeForgedTransactions(transactions: Collection<Transaction>) {
        internalStoreLock.withLock {
            for (t in transactions) {
                removeNoLock(t)
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
            logger.safeInfo { "Transaction ${transaction.id}: Not added because slot $slotHeight is full" }
            return true
        }

        return false
    }

    private fun tooManyTransactionsWithReferencedFullHash(transaction: Transaction): Boolean {
        if (transaction.referencedTransactionFullHash != null && maxPercentageUnconfirmedTransactionsFullHash <= (numberUnconfirmedTransactionsFullHash + 1) * 100 / maxSize) {
            logger.safeInfo { "Transaction ${transaction.id}: Not added because too many transactions with referenced full hash" }
            return true
        }

        return false
    }

    private fun cacheFullAndTransactionCheaperThanAllTheRest(transaction: Transaction): Boolean {
        if (amount == maxSize && internalStore.firstKey() > amountSlotForTransaction(transaction)) {
            logger.safeInfo { "Transaction ${transaction.id}: Not added because cache is full and transaction is cheaper than all the rest" }
            return true
        }

        return false
    }

    private fun transactionIsCurrentlyNotExpired(transaction: Transaction): Boolean {
        return if (dp.timeService.epochTime < transaction.expiration) {
            true
        } else {
            logger.safeInfo { "Transaction ${transaction.id} past expiration: ${transaction.expiration}" }
            false
        }
    }

    /**
     * Assumes locked.
     */
    private fun addTransaction(transaction: Transaction, peer: Peer?) {
        val slot = getOrCreateAmountSlotForTransaction(transaction)
        slot.add(transaction)
        amount++

        fingerPrintsOverview[transaction] = mutableSetOf()

        if (peer != null) {
            fingerPrintsOverview[transaction]!!.add(peer)
        }

        if (peer == null) {
            logger.safeDebug { "Adding Transaction ${transaction.id} from ourself" }
        } else {
            logger.safeDebug { "Adding Transaction ${transaction.id} from Peer ${peer.peerAddress}" }
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
        return transaction.feePlanck / Constants.FEE_QUANT
    }

    private fun removeCheapestFirstToExpireTransaction() {
        val cheapestFirstToExpireTransaction = this.internalStore[this.internalStore.firstKey()]!!
            .sortedWith(Comparator.comparingLong<Transaction> { it.feePlanck }
                .thenComparing<Int> { it.expiration }
                .thenComparing<Long> { it.id })
            .firstOrNull()
        if (cheapestFirstToExpireTransaction != null) {
            reservedBalanceCache.refundBalance(cheapestFirstToExpireTransaction)
            removeTransaction(cheapestFirstToExpireTransaction)
        }
    }

    /**
     * Assumes locked.
     */
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

    class ReservedBalanceCache(private val accountStore: AccountStore) {
        private val reservedBalanceCache = mutableMapOf<Long, Long>()

        fun reserveBalanceAndPut(transaction: Transaction) {
            var senderAccount: Account? = null

            if (transaction.senderId != 0L) {
                senderAccount = accountStore.accountTable[accountStore.accountKeyFactory.newKey(transaction.senderId)]
            }

            val amountPlanck = reservedBalanceCache.getOrDefault(transaction.senderId, 0L)
                .safeAdd(transaction.type.calculateTotalAmountPlanck(transaction))

            if (senderAccount == null) {
                logger.safeInfo {
                    String.format(
                        "Transaction %d: Account %d does not exist and has no balance. Required funds: %d",
                        transaction.id,
                        transaction.senderId,
                        amountPlanck
                    )
                }

                throw BurstException.NotCurrentlyValidException("Account unknown")
            } else if (amountPlanck > senderAccount.unconfirmedBalancePlanck) {
                logger.safeInfo {
                    String.format(
                        "Transaction %d: Account %d balance too low. You have  %d > %d Balance",
                        transaction.id,
                        transaction.senderId,
                        amountPlanck,
                        senderAccount.unconfirmedBalancePlanck
                    )
                }

                throw BurstException.NotCurrentlyValidException("Insufficient funds")
            }

            reservedBalanceCache[transaction.senderId] = amountPlanck
        }

        fun refundBalance(transaction: Transaction) {
            val amountPlanck = reservedBalanceCache.getOrDefault(transaction.senderId, 0L)
                .safeSubtract(transaction.type.calculateTotalAmountPlanck(transaction))

            if (amountPlanck > 0) {
                reservedBalanceCache[transaction.senderId] = amountPlanck
            } else {
                reservedBalanceCache.remove(transaction.senderId)
            }
        }

        fun rebuild(transactions: List<Transaction>): List<Transaction> {
            clear()

            val insufficientFundsTransactions = mutableListOf<Transaction>()

            for (t in transactions) {
                try {
                    this.reserveBalanceAndPut(t)
                } catch (e: BurstException.ValidationException) {
                    insufficientFundsTransactions.add(t)
                }

            }

            return insufficientFundsTransactions
        }

        fun clear() {
            reservedBalanceCache.clear()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UnconfirmedTransactionServiceImpl::class.java)
    }
}
