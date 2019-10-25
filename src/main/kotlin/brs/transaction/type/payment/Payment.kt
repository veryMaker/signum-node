package brs.transaction.type.payment

import brs.entity.Account
import brs.DependencyProvider
import brs.entity.Transaction
import brs.transaction.type.TransactionType

abstract class Payment(dp: DependencyProvider) : TransactionType(dp) {
    override val type = TYPE_PAYMENT

    override fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = true

    override fun applyAttachment(transaction: Transaction, senderAccount: Account, recipientAccount: Account?) = Unit

    override fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = Unit
}