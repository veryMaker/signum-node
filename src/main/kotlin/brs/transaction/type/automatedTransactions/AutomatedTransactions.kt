package brs.transaction.type.automatedTransactions

import brs.entity.Account
import brs.entity.DependencyProvider
import brs.entity.Transaction
import brs.transaction.type.TransactionType
import brs.util.BurstException

abstract class AutomatedTransactions(dp: DependencyProvider) : TransactionType(dp) {
    override val type = TYPE_AUTOMATED_TRANSACTIONS

    override fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = true

    override fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = Unit

    override fun validateAttachment(transaction: Transaction) {
        if (transaction.amountPlanck != 0L) {
            throw BurstException.NotValidException("Invalid automated transaction transaction")
        }
        doValidateAttachment(transaction)
    }

    internal abstract fun doValidateAttachment(transaction: Transaction)
}
