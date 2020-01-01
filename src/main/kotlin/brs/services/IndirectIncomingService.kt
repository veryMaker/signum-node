package brs.services

import brs.entity.Transaction

interface IndirectIncomingService {
    /**
     * Process a transaction to determine if the transaction results in any indirect incoming payments.
     */
    fun processTransaction(transaction: Transaction)

    /**
     * @return Whether the account with ID [accountId] is indirectly receiving from this transaction
     */
    fun isIndirectlyReceiving(transaction: Transaction, accountId: Long): Boolean
}
