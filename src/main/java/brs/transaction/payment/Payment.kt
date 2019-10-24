package brs.transaction.payment

import brs.Account
import brs.DependencyProvider
import brs.Transaction
import brs.transaction.TransactionType

abstract class Payment(dp: DependencyProvider) : TransactionType(dp) {
    override val type = TYPE_PAYMENT

    override fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = true

    override fun applyAttachment(transaction: Transaction, senderAccount: Account, recipientAccount: Account?) = Unit

    override fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = Unit
}