package brs.transaction.advancedPayment

import brs.*
import brs.transactionduplicates.TransactionDuplicationKey
import com.google.gson.JsonObject
import java.nio.ByteBuffer

class SubscriptionSubscribe(dp: DependencyProvider) : AdvancedPayment(dp) {
    override val subtype = SUBTYPE_ADVANCED_PAYMENT_SUBSCRIPTION_SUBSCRIBE
    override val description = "Subscription Subscribe"
    override fun parseAttachment(buffer: ByteBuffer, transactionVersion: Byte) = Attachment.AdvancedPaymentSubscriptionSubscribe(dp, buffer, transactionVersion)
    override fun parseAttachment(attachmentData: JsonObject) = Attachment.AdvancedPaymentSubscriptionSubscribe(dp, attachmentData)
    override fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = true

    override fun applyAttachment(
        transaction: Transaction,
        senderAccount: Account,
        recipientAccount: Account?
    ) {
        val attachment = transaction.attachment as Attachment.AdvancedPaymentSubscriptionSubscribe
        dp.subscriptionService.addSubscription(
            senderAccount,
            recipientAccount!!,
            transaction.id,
            transaction.amountNQT,
            transaction.timestamp,
            attachment.frequency!!
        )
    }

    override fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = Unit
    override fun getDuplicationKey(transaction: Transaction) = TransactionDuplicationKey.IS_NEVER_DUPLICATE

    override fun validateAttachment(transaction: Transaction) {
        val attachment = transaction.attachment as Attachment.AdvancedPaymentSubscriptionSubscribe
        if (attachment.frequency == null ||
            attachment.frequency < Constants.BURST_SUBSCRIPTION_MIN_Frequest ||
            attachment.frequency > Constants.BURST_SUBSCRIPTION_MAX_Frequest
        ) {
            throw BurstException.NotValidException("Invalid subscription frequency")
        }
        if (transaction.amountNQT < Constants.ONE_BURST || transaction.amountNQT > Constants.MAX_BALANCE_NQT) {
            throw BurstException.NotValidException("Subscriptions must be at least one burst")
        }
        if (transaction.senderId == transaction.recipientId) {
            throw BurstException.NotValidException("Cannot create subscription to same address")
        }
        if (!dp.subscriptionService.isEnabled()) {
            throw BurstException.NotYetEnabledException("Subscriptions not yet enabled")
        }
    }

    override fun hasRecipient() = true
}
