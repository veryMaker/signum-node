package brs.transaction.digitalGoods

import brs.*
import brs.transactionduplicates.TransactionDuplicationKey
import brs.util.convert.toUnsignedString
import brs.util.logging.safeTrace
import brs.util.toJsonString
import com.google.gson.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

class DigitalGoodsRefund(dp: DependencyProvider) : DigitalGoods(dp) {
    override val subtype = SUBTYPE_DIGITAL_GOODS_REFUND
    override val description = "Refund"
    override fun parseAttachment(buffer: ByteBuffer, transactionVersion: Byte) = Attachment.DigitalGoodsRefund(dp, buffer, transactionVersion)
    override fun parseAttachment(attachmentData: JsonObject) = Attachment.DigitalGoodsRefund(dp, attachmentData)

    override fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account): Boolean {
        logger.safeTrace { "TransactionType REFUND" }
        val totalAmountNQT = calculateAttachmentTotalAmountNQT(transaction)
        if (senderAccount.unconfirmedBalanceNQT >= totalAmountNQT) {
            dp.accountService.addToUnconfirmedBalanceNQT(senderAccount, -totalAmountNQT)
            return true
        }
        return false
    }

    public override fun calculateAttachmentTotalAmountNQT(transaction: Transaction): Long {
        val attachment = transaction.attachment as Attachment.DigitalGoodsRefund
        return attachment.refundNQT
    }

    override fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) {
        dp.accountService.addToUnconfirmedBalanceNQT(
            senderAccount,
            calculateAttachmentTotalAmountNQT(transaction)
        )
    }

    override fun applyAttachment(transaction: Transaction, senderAccount: Account, recipientAccount: Account?) {
        val attachment = transaction.attachment as Attachment.DigitalGoodsRefund
        dp.digitalGoodsStoreService.refund(
            transaction.senderId,
            attachment.purchaseId,
            attachment.refundNQT,
            transaction.encryptedMessage
        )
    }

    override fun doValidateAttachment(transaction: Transaction) {
        val attachment = transaction.attachment as Attachment.DigitalGoodsRefund
        val purchase = dp.digitalGoodsStoreService.getPurchase(attachment.purchaseId)
        if (attachment.refundNQT < 0 || attachment.refundNQT > Constants.MAX_BALANCE_NQT
            || purchase != null && (purchase.buyerId != transaction.recipientId || transaction.senderId != purchase.sellerId)
        ) {
            throw BurstException.NotValidException("Invalid digital goods refund: " + attachment.jsonObject.toJsonString())
        }
        if (transaction.encryptedMessage != null && !transaction.encryptedMessage.isText) {
            throw BurstException.NotValidException("Only text encrypted messages allowed")
        }
        if (purchase?.encryptedGoods == null || purchase.refundNQT != 0L) {
            throw BurstException.NotCurrentlyValidException("Purchase does not exist or is not delivered or is already refunded")
        }
    }

    override fun getDuplicationKey(transaction: Transaction): TransactionDuplicationKey {
        val attachment = transaction.attachment as Attachment.DigitalGoodsRefund
        return TransactionDuplicationKey(DigitalGoodsRefund::class, attachment.purchaseId.toUnsignedString())
    }

    override fun hasRecipient() = true

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DigitalGoodsRefund::class.java)
    }
}
