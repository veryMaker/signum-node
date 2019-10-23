package brs.transaction.payment

import brs.*
import brs.fluxcapacitor.FluxValues
import com.google.gson.JsonObject
import java.nio.ByteBuffer

class MultiOutPayment(dp: DependencyProvider) : Payment(dp) {
    override val subtype = SUBTYPE_PAYMENT_ORDINARY_PAYMENT_MULTI_OUT
    override val description = "Multi-out payment"

    override fun parseAttachment(
        buffer: ByteBuffer,
        transactionVersion: Byte
    ) = Attachment.PaymentMultiOutCreation(dp, buffer, transactionVersion)

    override fun parseAttachment(attachmentData: JsonObject) = Attachment.PaymentMultiOutCreation(dp, attachmentData)

    override suspend fun validateAttachment(transaction: Transaction) {
        if (!dp.fluxCapacitor.getValue(FluxValues.PRE_DYMAXION, transaction.height)) {
            throw BurstException.NotCurrentlyValidException("Multi Out Payments are not allowed before the Pre Dymaxion block")
        }

        val attachment = transaction.attachment as Attachment.PaymentMultiOutCreation
        val amountNQT = attachment.amountNQT
        if (amountNQT <= 0
            || amountNQT >= Constants.MAX_BALANCE_NQT
            || amountNQT != transaction.amountNQT
            || attachment.getRecipients().size < 2
        ) {
            throw BurstException.NotValidException("Invalid multi out payment")
        }
    }

    override suspend fun applyAttachment(
        transaction: Transaction,
        senderAccount: Account,
        recipientAccount: Account?
    ) {
        val attachment = transaction.attachment as Attachment.PaymentMultiOutCreation
        for (recipient in attachment.getRecipients()) {
            dp.accountService.addToBalanceAndUnconfirmedBalanceNQT(
                dp.accountService.getOrAddAccount(
                    recipient[0]
                ), recipient[1]
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