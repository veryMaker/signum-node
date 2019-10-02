package brs.transaction.advancedPayment

import brs.*
import brs.transactionduplicates.TransactionDuplicationKey
import com.google.gson.JsonObject
import java.nio.ByteBuffer

class EscrowResult(dp: DependencyProvider) : AdvancedPayment(dp) {
    override val subtype = SUBTYPE_ADVANCED_PAYMENT_ESCROW_RESULT
    override val description = "Escrow Result"
    override val isSigned = false
    override fun parseAttachment(buffer: ByteBuffer, transactionVersion: Byte) = Attachment.AdvancedPaymentEscrowResult(dp, buffer, transactionVersion)
    override fun parseAttachment(attachmentData: JsonObject) = Attachment.AdvancedPaymentEscrowResult(dp, attachmentData)
    override suspend fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = false
    override suspend fun applyAttachment(transaction: Transaction, senderAccount: Account, recipientAccount: Account?) = Unit
    override suspend fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = Unit
    override fun getDuplicationKey(transaction: Transaction) = TransactionDuplicationKey.IS_ALWAYS_DUPLICATE
    override fun validateAttachment(transaction: Transaction) = throw BurstException.NotValidException("Escrow result never validates")
    override fun hasRecipient(): Boolean = true
}
