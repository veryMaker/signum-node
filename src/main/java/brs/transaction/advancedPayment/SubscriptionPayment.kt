package brs.transaction.advancedPayment

import brs.*
import brs.transactionduplicates.TransactionDuplicationKey
import com.google.gson.JsonObject
import java.nio.ByteBuffer

class SubscriptionPayment(dp: DependencyProvider) : AdvancedPayment(dp) {
    override val subtype = SUBTYPE_ADVANCED_PAYMENT_SUBSCRIPTION_PAYMENT
    override val description = "Subscription Payment"
    override val isSigned = false
    override fun parseAttachment(buffer: ByteBuffer, transactionVersion: Byte) = Attachment.AdvancedPaymentSubscriptionPayment(dp, buffer, transactionVersion)
    override fun parseAttachment(attachmentData: JsonObject) = Attachment.AdvancedPaymentSubscriptionPayment(dp, attachmentData)
    override suspend fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = false
    override suspend fun applyAttachment(transaction: Transaction, senderAccount: Account, recipientAccount: Account?) = Unit
    override suspend fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = Unit
    override fun getDuplicationKey(transaction: Transaction) = TransactionDuplicationKey.IS_ALWAYS_DUPLICATE
    override suspend fun validateAttachment(transaction: Transaction) = throw BurstException.NotValidException("Subscription payment never validates")
    override fun hasRecipient() = true
}
