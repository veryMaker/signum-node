package brs.transaction.type.burstMining

import brs.entity.Account
import brs.entity.DependencyProvider
import brs.entity.Transaction
import brs.transaction.type.TransactionType

abstract class Mining(dp: DependencyProvider) : TransactionType(dp) {
    override val type = TYPE_MINING
    override fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = true
    override fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = Unit
}
