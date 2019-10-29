package brs.services.impl

import brs.entity.Transaction
import brs.entity.TransactionDuplicationKey
import brs.entity.TransactionDuplicationResult
import brs.services.TransactionDuplicateCheckerService
import brs.transaction.type.TransactionType
import kotlin.reflect.KClass

inline class TransactionDuplicateCheckerServiceImpl(private val duplicates: MutableMap<KClass<out TransactionType>, MutableMap<String, Transaction>> = mutableMapOf()) :
    TransactionDuplicateCheckerService {
    override fun clear() {
        duplicates.clear()
    }

    override fun removeCheaperDuplicate(transaction: Transaction): TransactionDuplicationResult {
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
            TransactionDuplicationResult(true, transaction)
        } else {
            transactionOverview[transactionDuplicateKey.key] = transaction
            TransactionDuplicationResult(possiblyExistingTransaction != null, possiblyExistingTransaction)
        }
    }

    override fun hasAnyDuplicate(transaction: Transaction): Boolean {
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

    override fun removeTransaction(transaction: Transaction) {
        val transactionDuplicateKey = transaction.duplicationKey
        val map = duplicates[transactionDuplicateKey.transactionType] ?: return
        if (transactionDuplicateKey != TransactionDuplicationKey.IS_ALWAYS_DUPLICATE && transactionDuplicateKey != TransactionDuplicationKey.IS_NEVER_DUPLICATE && duplicates.containsKey(
                transactionDuplicateKey.transactionType
            ) && map[transactionDuplicateKey.key] == transaction
        ) {
            map.remove(transactionDuplicateKey.key)
        }
    }
}
