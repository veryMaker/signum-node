package brs.services

import brs.entity.Transaction

interface TransactionService {
    /**
     * TODO
     */
    fun verifyPublicKey(transaction: Transaction): Boolean

    /**
     * TODO
     */
    fun validate(transaction: Transaction)

    /**
     * TODO
     */
    fun applyUnconfirmed(transaction: Transaction): Boolean

    /**
     * TODO
     */
    fun apply(transaction: Transaction)

    /**
     * TODO
     */
    fun undoUnconfirmed(transaction: Transaction)
}
