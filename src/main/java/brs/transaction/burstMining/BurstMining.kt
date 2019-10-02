package brs.transaction.burstMining

import brs.Account
import brs.DependencyProvider
import brs.Transaction
import brs.transaction.TransactionType

abstract class BurstMining(dp: DependencyProvider) : TransactionType(dp) { // TODO rename to just Mining
    override val type = TYPE_BURST_MINING
    override suspend fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = true
    override suspend fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = Unit
}
