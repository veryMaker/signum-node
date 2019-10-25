package brs.transaction.type.messaging

import brs.*
import brs.entity.Account
import brs.objects.FluxValues
import brs.entity.Transaction
import brs.transaction.appendix.Appendix
import brs.transaction.appendix.Attachment
import brs.util.BurstException
import brs.util.toJsonString
import com.google.gson.JsonObject
import java.nio.ByteBuffer

class ArbitraryMessage(dp: DependencyProvider) : Messaging(dp) {
    override val subtype = SUBTYPE_MESSAGING_ARBITRARY_MESSAGE
    override val description = "Arbitrary Message"

    override fun parseAttachment(buffer: ByteBuffer, transactionVersion: Byte) = Attachment.ArbitraryMessage(dp)

    override fun parseAttachment(attachmentData: JsonObject) = Attachment.ArbitraryMessage(dp)

    override fun applyAttachment(
        transaction: Transaction,
        senderAccount: Account,
        recipientAccount: Account?
    ) {
        // No appendices
    }

    override fun validateAttachment(transaction: Transaction) {
        val attachment = transaction.attachment
        if (transaction.amountPlanck != 0L) {
            throw BurstException.NotValidException("Invalid arbitrary message: " + attachment.jsonObject.toJsonString())
        }
        if (!dp.fluxCapacitorService.getValue(FluxValues.DIGITAL_GOODS_STORE) && transaction.message == null) {
            throw BurstException.NotCurrentlyValidException("Missing message appendix not allowed before DGS block")
        }
    }

    override fun hasRecipient() = true

    override fun parseAppendices(
        builder: Transaction.Builder,
        flags: Int,
        version: Byte,
        buffer: ByteBuffer
    ) {
        var position = 1
        if (flags and position != 0 || version.toInt() == 0) {
            builder.message(Appendix.Message(buffer, version))
        }
        position = position shl 1
        if (flags and position != 0) {
            builder.encryptedMessage(Appendix.EncryptedMessage(buffer, version))
        }
        position = position shl 1
        if (flags and position != 0) {
            builder.publicKeyAnnouncement(Appendix.PublicKeyAnnouncement(dp, buffer, version))
        }
        position = position shl 1
        if (flags and position != 0) {
            builder.encryptToSelfMessage(Appendix.EncryptToSelfMessage(buffer, version))
        }
    }
}
