package brs.services

import brs.Transaction

interface IndirectIncomingService {
    fun processTransaction(transaction: Transaction)
    fun isIndirectlyReceiving(transaction: Transaction, accountId: Long): Boolean
}
