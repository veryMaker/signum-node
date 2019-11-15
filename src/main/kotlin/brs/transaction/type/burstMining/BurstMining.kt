package brs.transaction.type.burstMining

import brs.entity.Account
import brs.entity.DependencyProvider
import brs.entity.Transaction
import brs.transaction.type.TransactionType

abstract class BurstMining(dp: DependencyProvider) : TransactionType(dp) { // TODO rename to just Mining
    override val type = TYPE_BURST_MINING
    override fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = true
    override fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = Unit
}
