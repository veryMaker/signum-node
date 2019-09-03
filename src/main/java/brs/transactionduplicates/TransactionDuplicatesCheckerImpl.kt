package brs.transactionduplicates

import brs.Transaction
import brs.TransactionType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.HashMap
import java.util.Objects

class TransactionDuplicatesCheckerImpl {

    private val logger = LoggerFactory.getLogger(TransactionDuplicatesCheckerImpl::class.java)

    private val duplicates = mutableMapOf<TransactionType, MutableMap<String, Transaction>>()

    fun clear() {
        duplicates.clear()
    }

    fun removeCheaperDuplicate(transaction: Transaction): TransactionDuplicationResult {
        val transactionDuplicateKey = transaction.duplicationKey

        if (transactionDuplicateKey == TransactionDuplicationKey.IS_ALWAYS_DUPLICATE) {
            return TransactionDuplicationResult(true, null)
        } else if (transactionDuplicateKey == TransactionDuplicationKey.IS_NEVER_DUPLICATE) {
            return TransactionDuplicationResult(false, null)
        }

        duplicates.computeIfAbsent(transactionDuplicateKey.transactionType) { mutableMapOf() }

        val transactionOverview = duplicates[transactionDuplicateKey.transactionType]!!

        val possiblyExistingTransaction = transactionOverview[transactionDuplicateKey.key]

        return if (possiblyExistingTransaction != null && possiblyExistingTransaction.feeNQT >= transaction.feeNQT) {
            logger.debug("Transaction {}: is a duplicate of {} (Type: {})", transaction.id, possiblyExistingTransaction.id, transaction.type)
            TransactionDuplicationResult(true, transaction)
        } else {
            transactionOverview[transactionDuplicateKey.key] = transaction
            TransactionDuplicationResult(possiblyExistingTransaction != null, possiblyExistingTransaction)
        }
    }

    fun hasAnyDuplicate(transaction: Transaction): Boolean {
        val transactionDuplicateKey = transaction.duplicationKey

        if (transactionDuplicateKey == TransactionDuplicationKey.IS_ALWAYS_DUPLICATE) {
            return true
        } else if (transactionDuplicateKey == TransactionDuplicationKey.IS_NEVER_DUPLICATE) {
            return false
        }

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

        if (transactionDuplicateKey != TransactionDuplicationKey.IS_ALWAYS_DUPLICATE && transactionDuplicateKey != TransactionDuplicationKey.IS_NEVER_DUPLICATE && duplicates.containsKey(transactionDuplicateKey.transactionType) && duplicates[transactionDuplicateKey.transactionType][transactionDuplicateKey.key] == transaction) {
            duplicates[transactionDuplicateKey.transactionType].remove(transactionDuplicateKey.key)
        }
    }
}
