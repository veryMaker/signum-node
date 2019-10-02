package brs.transaction.automatedTransactions

import brs.Account
import brs.BurstException
import brs.DependencyProvider
import brs.Transaction
import brs.transaction.TransactionType

abstract class AutomatedTransactions(dp: DependencyProvider) : TransactionType(dp) {
    override val type = TYPE_AUTOMATED_TRANSACTIONS

    override suspend fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = true

    override suspend fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = Unit

    override fun validateAttachment(transaction: Transaction) {
        if (transaction.amountNQT != 0L) {
            throw BurstException.NotValidException("Invalid automated transaction transaction")
        }
        doValidateAttachment(transaction)
    }

    internal abstract fun doValidateAttachment(transaction: Transaction)
}
