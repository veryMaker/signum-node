package brs.services

import brs.entity.Transaction
import brs.entity.TransactionDuplicationResult

interface TransactionDuplicateCheckerService {
    /**
     * TODO
     */
    fun clear()

    /**
     * TODO
     */
    fun removeCheaperDuplicate(transaction: Transaction): TransactionDuplicationResult

    /**
     * TODO
     */
    fun hasAnyDuplicate(transaction: Transaction): Boolean

    /**
     * TODO
     */
    fun removeTransaction(transaction: Transaction)
}