package brs.services

import brs.entity.Transaction

interface TransactionService { // TODO combine with transactionprocessor
    fun verifyPublicKey(transaction: Transaction): Boolean

    fun validate(transaction: Transaction)

    fun applyUnconfirmed(transaction: Transaction): Boolean

    fun apply(transaction: Transaction)

    fun undoUnconfirmed(transaction: Transaction)
}
