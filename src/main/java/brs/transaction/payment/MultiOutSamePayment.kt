package brs.transaction.payment

import brs.*
import brs.fluxcapacitor.FluxValues
import brs.util.convert.safeDivide
import com.google.gson.JsonObject
import java.nio.ByteBuffer

class MultiOutSamePayment(dp: DependencyProvider) : Payment(dp) {
    override val subtype = SUBTYPE_PAYMENT_ORDINARY_PAYMENT_MULTI_SAME_OUT
    override val description = "Multi-out Same Payment"

    override fun parseAttachment(
        buffer: ByteBuffer,
        transactionVersion: Byte
    ) = Attachment.PaymentMultiSameOutCreation(dp, buffer, transactionVersion)

    override fun parseAttachment(attachmentData: JsonObject) = Attachment.PaymentMultiSameOutCreation(dp, attachmentData)

    override suspend fun validateAttachment(transaction: Transaction) {
        if (!dp.fluxCapacitor.getValue(FluxValues.PRE_DYMAXION, transaction.height)) {
            throw BurstException.NotCurrentlyValidException("Multi Same Out Payments are not allowed before the Pre Dymaxion block")
        }

        val attachment = transaction.attachment as Attachment.PaymentMultiSameOutCreation
        if (attachment.getRecipients().size < 2 && transaction.amountNQT % attachment.getRecipients().size == 0L) {
            throw BurstException.NotValidException("Invalid multi out payment")
        }
    }

    override suspend fun applyAttachment(
        transaction: Transaction,
        senderAccount: Account,
        recipientAccount: Account?
    ) {
        val attachment = transaction.attachment as Attachment.PaymentMultiSameOutCreation
        val amountNQT = transaction.amountNQT.safeDivide(attachment.getRecipients().size.toLong())
        attachment.getRecipients().forEach { a ->
            dp.accountService.addToBalanceAndUnconfirmedBalanceNQT(
                dp.accountService.getOrAddAccount(a), amountNQT
            )
        }
    }

    override fun hasRecipient() = false

    override fun parseAppendices(builder: Transaction.Builder, attachmentData: JsonObject) = Unit

    override fun parseAppendices(
        builder: Transaction.Builder,
        flags: Int,
        version: Byte,
        buffer: ByteBuffer
    ) = Unit
}
