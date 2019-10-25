package brs.transaction.type.accountControl

import brs.entity.Account
import brs.DependencyProvider
import brs.entity.Transaction
import brs.transaction.type.TransactionType

abstract class AccountControl(dp: DependencyProvider) : TransactionType(dp) {
    override val type = TYPE_ACCOUNT_CONTROL
    override fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = true
    override fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = Unit
}