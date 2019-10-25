package brs.services

import brs.Transaction

interface TransactionService {
    fun verifyPublicKey(transaction: Transaction): Boolean

    fun validate(transaction: Transaction)

    fun applyUnconfirmed(transaction: Transaction): Boolean

    fun apply(transaction: Transaction)

    fun undoUnconfirmed(transaction: Transaction)
}
