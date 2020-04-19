package brs.services.impl

import brs.entity.DependencyProvider
import brs.entity.IndirectIncoming
import brs.entity.Transaction
import brs.services.IndirectIncomingService
import brs.transaction.appendix.Attachment
import brs.transaction.type.payment.MultiOutPayment
import brs.transaction.type.payment.MultiOutSamePayment

class IndirectIncomingServiceImpl(private val dp: DependencyProvider) : IndirectIncomingService {
    override fun processTransaction(transaction: Transaction) {
        dp.db.indirectIncomingStore.addIndirectIncomings(getIndirectIncomings(transaction)
            .map { account -> IndirectIncoming(account, transaction.id, transaction.height) })
    }

    override fun isIndirectlyReceiving(transaction: Transaction, accountId: Long): Boolean {
        // It would be confusing to have inconsistent behaviour so even when not loading from database we should disable when told to do so.
        return getIndirectIncomings(transaction).contains(accountId)
    }

    private fun getIndirectIncomings(transaction: Transaction): Collection<Long> {
        return when (transaction.type) {
            is MultiOutPayment -> getMultiOutRecipients(transaction)
            is MultiOutSamePayment -> getMultiOutSameRecipients(transaction)
            else -> emptyList()
        }
    }

    private fun getMultiOutRecipients(transaction: Transaction): Collection<Long> {
        require(transaction.type is MultiOutPayment && transaction.attachment is Attachment.PaymentMultiOutCreation) { "Wrong transaction type" }

        val attachment = transaction.attachment
        return attachment.getRecipients()
            .map { recipient -> recipient[0] }
    }

    private fun getMultiOutSameRecipients(transaction: Transaction): Collection<Long> {
        require(transaction.type is MultiOutSamePayment && transaction.attachment is Attachment.PaymentMultiSameOutCreation) { "Wrong transaction type" }

        val attachment = transaction.attachment
        return attachment.getRecipients()
    }
}
