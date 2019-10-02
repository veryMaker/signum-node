package brs.transaction.messaging

import brs.Account
import brs.DependencyProvider
import brs.Transaction
import brs.transaction.TransactionType

abstract class Messaging(dp: DependencyProvider) : TransactionType(dp) {
    override val type = TYPE_MESSAGING
    override suspend fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = true
    override suspend fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = Unit
}