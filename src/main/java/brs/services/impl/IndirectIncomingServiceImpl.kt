package brs.services.impl

import brs.Attachment
import brs.DependencyProvider
import brs.Transaction
import brs.TransactionType
import brs.db.store.IndirectIncomingStore
import brs.props.Props
import brs.services.IndirectIncomingService
import org.slf4j.LoggerFactory

class IndirectIncomingServiceImpl(private val dp: DependencyProvider) : IndirectIncomingService {
    private val disabled = !dp.propertyService.get(Props.INDIRECT_INCOMING_SERVICE_ENABLE)

    init {
        if (disabled) {
            LOGGER.warn("Indirect Incoming Service Disabled!")
        }
    }

    override fun processTransaction(transaction: Transaction) {
        if (disabled) return
        dp.indirectIncomingStore.addIndirectIncomings(getIndirectIncomings(transaction)
                .map { account -> IndirectIncomingStore.IndirectIncoming(account!!, transaction.id, transaction.height) })
    }

    override fun isIndirectlyReceiving(transaction: Transaction, accountId: Long): Boolean {
        // It would be confusing to have inconsistent behaviour so even when not loading from database we should disable when told to do so.
        return !disabled && getIndirectIncomings(transaction).contains(accountId)
    }

    private fun getIndirectIncomings(transaction: Transaction): Collection<Long> {
        return if (transaction.type == TransactionType.Payment.MULTI_OUT) {
            getMultiOutRecipients(transaction)
        } else if (transaction.type == TransactionType.Payment.MULTI_SAME_OUT) {
            getMultiOutSameRecipients(transaction)
        } else {
            emptyList()
        }
    }

    private fun getMultiOutRecipients(transaction: Transaction): Collection<Long> {
        require(!(transaction.type != TransactionType.Payment.MULTI_OUT || transaction.attachment !is Attachment.PaymentMultiOutCreation)) { "Wrong transaction type" }

        val attachment = transaction.attachment
        return attachment.getRecipients()
                .map { recipient -> recipient[0] }
    }

    private fun getMultiOutSameRecipients(transaction: Transaction): Collection<Long> {
        require(!(transaction.type != TransactionType.Payment.MULTI_SAME_OUT || transaction.attachment !is Attachment.PaymentMultiSameOutCreation)) { "Wrong transaction type" }

        val attachment = transaction.attachment
        return attachment.getRecipients()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(IndirectIncomingServiceImpl::class.java)
    }
}
