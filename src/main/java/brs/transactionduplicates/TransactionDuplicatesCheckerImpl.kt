package brs.transactionduplicates

import brs.Transaction
import brs.TransactionType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.HashMap
import java.util.Objects

class TransactionDuplicatesCheckerImpl {

    private val logger = LoggerFactory.getLogger(TransactionDuplicatesCheckerImpl::class.java)

    private val duplicates = HashMap<TransactionType, HashMap<String, Transaction>>()

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

        (duplicates as java.util.Map<TransactionType, HashMap<String, Transaction>>).computeIfAbsent(transactionDuplicateKey.transactionType) { n -> HashMap() }

        val transactionOverview = duplicates[transactionDuplicateKey.transactionType]

        val possiblyExistingTransaction = transactionOverview[transactionDuplicateKey.key]

        if (possiblyExistingTransaction != null && possiblyExistingTransaction.feeNQT >= transaction.feeNQT) {
            logger.debug("Transaction {}: is a duplicate of {} (Type: {})", transaction.id, possiblyExistingTransaction.id, transaction.type)
            return TransactionDuplicationResult(true, transaction)
        } else {
            transactionOverview[transactionDuplicateKey.key] = transaction
            return TransactionDuplicationResult(possiblyExistingTransaction != null, possiblyExistingTransaction)
        }
    }

    fun hasAnyDuplicate(transaction: Transaction): Boolean {
        val transactionDuplicateKey = transaction.duplicationKey

        if (transactionDuplicateKey == TransactionDuplicationKey.IS_ALWAYS_DUPLICATE) {
            return true
        } else if (transactionDuplicateKey == TransactionDuplicationKey.IS_NEVER_DUPLICATE) {
            return false
        }

        (duplicates as java.util.Map<TransactionType, HashMap<String, Transaction>>).computeIfAbsent(transactionDuplicateKey.transactionType) { n -> HashMap() }

        val transactionOverview = duplicates[transactionDuplicateKey.transactionType]

        if (transactionOverview.containsKey(transactionDuplicateKey.key)) {
            return true
        } else {
            transactionOverview[transactionDuplicateKey.key] = transaction
            return false
        }
    }

    fun removeTransaction(transaction: Transaction) {
        val transactionDuplicateKey = transaction.duplicationKey

        if (transactionDuplicateKey != TransactionDuplicationKey.IS_ALWAYS_DUPLICATE && transactionDuplicateKey != TransactionDuplicationKey.IS_NEVER_DUPLICATE && duplicates.containsKey(transactionDuplicateKey.transactionType) && duplicates[transactionDuplicateKey.transactionType][transactionDuplicateKey.key] == transaction) {
            duplicates[transactionDuplicateKey.transactionType].remove(transactionDuplicateKey.key)
        }
    }
}
