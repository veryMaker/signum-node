package brs.transaction.digitalGoods

import brs.*
import brs.transactionduplicates.TransactionDuplicationKey
import brs.util.toJsonString
import brs.util.toUnsignedString
import com.google.gson.JsonObject
import java.nio.ByteBuffer

class DigitalGoodsFeedback(dp: DependencyProvider) : DigitalGoods(dp) {
    override val subtype = SUBTYPE_DIGITAL_GOODS_FEEDBACK
    override val description = "Feedback"
    override fun parseAttachment(buffer: ByteBuffer, transactionVersion: Byte) = Attachment.DigitalGoodsFeedback(dp, buffer, transactionVersion)
    override fun parseAttachment(attachmentData: JsonObject) = Attachment.DigitalGoodsFeedback(dp, attachmentData)

    override fun applyAttachment(transaction: Transaction, senderAccount: Account, recipientAccount: Account?) {
        val attachment = transaction.attachment as Attachment.DigitalGoodsFeedback
        dp.digitalGoodsStoreService.feedback(
            attachment.purchaseId,
            transaction.encryptedMessage!!,
            transaction.message!!
        )
    }

    override fun doValidateAttachment(transaction: Transaction) {
        val attachment = transaction.attachment as Attachment.DigitalGoodsFeedback
        val purchase = dp.digitalGoodsStoreService.getPurchase(attachment.purchaseId)
        if (purchase != null && (purchase.sellerId != transaction.recipientId || transaction.senderId != purchase.buyerId)) {
            throw BurstException.NotValidException("Invalid digital goods feedback: " + attachment.jsonObject.toJsonString())
        }
        if (transaction.encryptedMessage == null && transaction.message == null) {
            throw BurstException.NotValidException("Missing feedback message")
        }
        if (transaction.encryptedMessage != null && !transaction.encryptedMessage.isText) {
            throw BurstException.NotValidException("Only text encrypted messages allowed")
        }
        if (transaction.message != null && !transaction.message.isText) {
            throw BurstException.NotValidException("Only text public messages allowed")
        }
        if (purchase?.encryptedGoods == null) {
            throw BurstException.NotCurrentlyValidException("Purchase does not exist yet or not yet delivered")
        }
    }

    override fun getDuplicationKey(transaction: Transaction): TransactionDuplicationKey {
        val attachment = transaction.attachment as Attachment.DigitalGoodsFeedback
        return TransactionDuplicationKey(DigitalGoodsFeedback::class, attachment.purchaseId.toUnsignedString())
    }

    override fun hasRecipient() = true
}
