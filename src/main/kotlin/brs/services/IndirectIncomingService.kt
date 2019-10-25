package brs.services

import brs.entity.Transaction

interface IndirectIncomingService {
    fun processTransaction(transaction: Transaction)
    fun isIndirectlyReceiving(transaction: Transaction, accountId: Long): Boolean
}
