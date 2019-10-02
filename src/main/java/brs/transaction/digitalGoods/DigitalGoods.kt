package brs.transaction.digitalGoods

import brs.Account
import brs.BurstException
import brs.DependencyProvider
import brs.Transaction
import brs.fluxcapacitor.FluxValues
import brs.transaction.TransactionType

abstract class DigitalGoods(dp: DependencyProvider) : TransactionType(dp) {
    override val type = TYPE_DIGITAL_GOODS
    override suspend fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = true
    override suspend fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = Unit
    internal abstract fun doValidateAttachment(transaction: Transaction)
    override fun validateAttachment(transaction: Transaction) {
        if (!dp.fluxCapacitor.getValue(FluxValues.DIGITAL_GOODS_STORE, dp.blockchain.lastBlock.height)) {
            throw BurstException.NotYetEnabledException("Digital goods listing not yet enabled at height " + dp.blockchain.lastBlock.height)
        }
        if (transaction.amountNQT != 0L) {
            throw BurstException.NotValidException("Invalid digital goods transaction")
        }
        doValidateAttachment(transaction)
    }
}
