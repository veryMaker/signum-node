package brs.transactionduplicates

import brs.Transaction
import brs.transaction.TransactionType
import brs.util.logging.safeDebug
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

class TransactionDuplicatesCheckerImpl {

    private val logger = LoggerFactory.getLogger(TransactionDuplicatesCheckerImpl::class.java)

    private val duplicates = mutableMapOf<KClass<out TransactionType>, MutableMap<String, Transaction>>()

    fun clear() {
        duplicates.clear()
    }

    fun removeCheaperDuplicate(transaction: Transaction): TransactionDuplicationResult {
        val transactionDuplicateKey = transaction.duplicationKey

        when (transactionDuplicateKey) {
            TransactionDuplicationKey.IS_ALWAYS_DUPLICATE -> return TransactionDuplicationResult(true, null)
            TransactionDuplicationKey.IS_NEVER_DUPLICATE -> return TransactionDuplicationResult(false, null)
        }

        requireNotNull(transactionDuplicateKey.transactionType) { "Transaction Type of null is only allowed for IS_ALWAYS_DUPLICATE and IS_NEVER_DUPLICATE" }

        duplicates.computeIfAbsent(transactionDuplicateKey.transactionType) { mutableMapOf() }

        val transactionOverview = duplicates[transactionDuplicateKey.transactionType]!!

        val possiblyExistingTransaction = transactionOverview[transactionDuplicateKey.key]

        return if (possiblyExistingTransaction != null && possiblyExistingTransaction.feePlanck >= transaction.feePlanck) {
            logger.safeDebug { "Transaction ${transaction.id}: is a duplicate of ${possiblyExistingTransaction.id} (Type: ${transaction.type})" }
            TransactionDuplicationResult(true, transaction)
        } else {
            transactionOverview[transactionDuplicateKey.key] = transaction
            TransactionDuplicationResult(possiblyExistingTransaction != null, possiblyExistingTransaction)
        }
    }

    fun hasAnyDuplicate(transaction: Transaction): Boolean {
        val transactionDuplicateKey = transaction.duplicationKey

        when (transactionDuplicateKey) {
            TransactionDuplicationKey.IS_ALWAYS_DUPLICATE -> return true
            TransactionDuplicationKey.IS_NEVER_DUPLICATE -> return false
        }

        requireNotNull(transactionDuplicateKey.transactionType) { "Transaction Type of null is only allowed for IS_ALWAYS_DUPLICATE and IS_NEVER_DUPLICATE" }

        duplicates.computeIfAbsent(transactionDuplicateKey.transactionType) { mutableMapOf() }

        val transactionOverview = duplicates[transactionDuplicateKey.transactionType]!!

        return if (transactionOverview.containsKey(transactionDuplicateKey.key)) {
            true
        } else {
            transactionOverview[transactionDuplicateKey.key] = transaction
            false
        }
    }

    fun removeTransaction(transaction: Transaction) {
        val transactionDuplicateKey = transaction.duplicationKey
        val map = duplicates[transactionDuplicateKey.transactionType] ?: return
        if (transactionDuplicateKey != TransactionDuplicationKey.IS_ALWAYS_DUPLICATE && transactionDuplicateKey != TransactionDuplicationKey.IS_NEVER_DUPLICATE && duplicates.containsKey(transactionDuplicateKey.transactionType) && map[transactionDuplicateKey.key] == transaction) {
            map.remove(transactionDuplicateKey.key)
        }
    }
}
