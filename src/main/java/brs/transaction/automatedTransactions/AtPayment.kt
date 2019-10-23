package brs.transaction.automatedTransactions

import brs.*
import com.google.gson.JsonObject
import java.nio.ByteBuffer

class AtPayment(dp: DependencyProvider) : AutomatedTransactions(dp) {
    override val subtype = SUBTYPE_AT_PAYMENT
    override val description = "AT Payment"
    override val isSigned = false
    override fun parseAttachment(buffer: ByteBuffer, transactionVersion: Byte) = Attachment.AtPayment(dp)
    override fun parseAttachment(attachmentData: JsonObject) = Attachment.AtPayment(dp)
    override suspend fun doValidateAttachment(transaction: Transaction) = throw BurstException.NotValidException("AT payment never validates")
    override suspend fun applyAttachment(transaction: Transaction, senderAccount: Account, recipientAccount: Account?) = Unit
    override fun hasRecipient() = true
}
