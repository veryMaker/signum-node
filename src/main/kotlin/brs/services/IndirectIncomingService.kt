package brs.services

import brs.entity.Transaction

interface IndirectIncomingService {
    /**
     * TODO
     */
    fun processTransaction(transaction: Transaction)

    /**
     * TODO
     */
    fun isIndirectlyReceiving(transaction: Transaction, accountId: Long): Boolean
}
