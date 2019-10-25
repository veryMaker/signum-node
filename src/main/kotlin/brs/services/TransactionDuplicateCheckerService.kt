package brs.services

import brs.entity.Transaction
import brs.entity.TransactionDuplicationResult

interface TransactionDuplicateCheckerService {
    fun clear()
    fun removeCheaperDuplicate(transaction: Transaction): TransactionDuplicationResult
    fun hasAnyDuplicate(transaction: Transaction): Boolean
    fun removeTransaction(transaction: Transaction)
}