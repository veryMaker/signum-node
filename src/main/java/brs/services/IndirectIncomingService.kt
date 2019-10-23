package brs.services

import brs.Transaction

interface IndirectIncomingService {
    suspend fun processTransaction(transaction: Transaction)
    fun isIndirectlyReceiving(transaction: Transaction, accountId: Long): Boolean
}
