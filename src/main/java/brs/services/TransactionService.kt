package brs.services

import brs.Transaction

interface TransactionService {
    fun verifyPublicKey(transaction: Transaction): Boolean

    fun validate(transaction: Transaction)

    suspend fun applyUnconfirmed(transaction: Transaction): Boolean

    suspend fun apply(transaction: Transaction)

    suspend fun undoUnconfirmed(transaction: Transaction)
}
