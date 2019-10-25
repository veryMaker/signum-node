package brs.services.impl

import brs.transaction.appendix.Attachment
import brs.entity.DependencyProvider
import brs.entity.Transaction
import brs.db.IndirectIncomingStore
import brs.objects.Props
import brs.services.IndirectIncomingService
import brs.transaction.type.payment.MultiOutPayment
import brs.transaction.type.payment.MultiOutSamePayment
import brs.util.logging.safeWarn
import org.slf4j.LoggerFactory

class IndirectIncomingServiceImpl(private val dp: DependencyProvider) : IndirectIncomingService {
    private val disabled = !dp.propertyService.get(Props.INDIRECT_INCOMING_SERVICE_ENABLE)

    init {
        if (disabled) {
            logger.safeWarn { "Indirect Incoming Service Disabled!" }
        }
    }

    override fun processTransaction(transaction: Transaction) {
        if (disabled) return
        dp.indirectIncomingStore.addIndirectIncomings(getIndirectIncomings(transaction)
                .map { account -> IndirectIncomingStore.IndirectIncoming(account, transaction.id, transaction.height) })
    }

    override fun isIndirectlyReceiving(transaction: Transaction, accountId: Long): Boolean {
        // It would be confusing to have inconsistent behaviour so even when not loading from database we should disable when told to do so.
        return !disabled && getIndirectIncomings(transaction).contains(accountId)
    }

    private fun getIndirectIncomings(transaction: Transaction): Collection<Long> {
        return when {
            transaction.type is MultiOutPayment -> getMultiOutRecipients(transaction)
            transaction.type is MultiOutSamePayment -> getMultiOutSameRecipients(transaction)
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

    companion object {
        private val logger = LoggerFactory.getLogger(IndirectIncomingServiceImpl::class.java)
    }
}
