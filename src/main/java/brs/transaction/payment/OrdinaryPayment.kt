package brs.transaction.payment

import brs.*
import com.google.gson.JsonObject
import java.nio.ByteBuffer

class OrdinaryPayment(dp: DependencyProvider) : Payment(dp) {
    override val subtype = SUBTYPE_PAYMENT_ORDINARY_PAYMENT
    override val description = "Ordinary Payment"

    override fun parseAttachment(buffer: ByteBuffer, transactionVersion: Byte) = Attachment.OrdinaryPayment(dp)

    override fun parseAttachment(attachmentData: JsonObject) = Attachment.OrdinaryPayment(dp)

    override suspend fun validateAttachment(transaction: Transaction) {
        if (transaction.amountNQT <= 0 || transaction.amountNQT >= Constants.MAX_BALANCE_NQT) {
            throw BurstException.NotValidException("Invalid ordinary payment")
        }
    }

    override fun hasRecipient() = true
}
