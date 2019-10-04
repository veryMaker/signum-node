package brs.transaction.digitalGoods

import brs.*
import brs.util.convert.safeMultiply
import brs.util.convert.toUnsignedString
import brs.util.logging.safeTrace
import brs.util.toJsonString
import com.google.gson.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

class DigitalGoodsPurchase(dp: DependencyProvider) : DigitalGoods(dp) {
    override val subtype = SUBTYPE_DIGITAL_GOODS_PURCHASE
    override val description = "Purchase"
    override fun parseAttachment(buffer: ByteBuffer, transactionVersion: Byte) = Attachment.DigitalGoodsPurchase(dp, buffer, transactionVersion)
    override fun parseAttachment(attachmentData: JsonObject) = Attachment.DigitalGoodsPurchase(dp, attachmentData)

    override suspend fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account): Boolean {
        logger.safeTrace { "TransactionType PURCHASE" }
        val totalAmountNQT = calculateAttachmentTotalAmountNQT(transaction)
        if (senderAccount.unconfirmedBalanceNQT >= totalAmountNQT) {
            dp.accountService.addToUnconfirmedBalanceNQT(senderAccount, -totalAmountNQT)
            return true
        }
        return false
    }

    public override fun calculateAttachmentTotalAmountNQT(transaction: Transaction): Long {
        val attachment = transaction.attachment as Attachment.DigitalGoodsPurchase
        return attachment.quantity.toLong().safeMultiply(attachment.priceNQT)
    }

    override suspend fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) {
        dp.accountService.addToUnconfirmedBalanceNQT(
            senderAccount,
            calculateAttachmentTotalAmountNQT(transaction)
        )
    }

    override suspend fun applyAttachment(transaction: Transaction, senderAccount: Account, recipientAccount: Account?) {
        val attachment = transaction.attachment as Attachment.DigitalGoodsPurchase
        dp.digitalGoodsStoreService.purchase(transaction, attachment)
    }

    override fun doValidateAttachment(transaction: Transaction) {
        val attachment = transaction.attachment as Attachment.DigitalGoodsPurchase
        val goods = dp.digitalGoodsStoreService.getGoods(attachment.goodsId)
        if (attachment.quantity <= 0 || attachment.quantity > Constants.MAX_DGS_LISTING_QUANTITY
            || attachment.priceNQT <= 0 || attachment.priceNQT > Constants.MAX_BALANCE_NQT
            || goods != null && goods.sellerId != transaction.recipientId
        ) {
            throw BurstException.NotValidException("Invalid digital goods purchase: " + attachment.jsonObject.toJsonString())
        }
        if (transaction.encryptedMessage != null && !transaction.encryptedMessage.isText) {
            throw BurstException.NotValidException("Only text encrypted messages allowed")
        }
        if (goods == null || goods.isDelisted) {
            throw BurstException.NotCurrentlyValidException(
                "Goods " + attachment.goodsId.toUnsignedString() +
                        "not yet listed or already delisted"
            )
        }
        if (attachment.quantity > goods.quantity || attachment.priceNQT != goods.priceNQT) {
            throw BurstException.NotCurrentlyValidException("Goods price or quantity changed: " + attachment.jsonObject.toJsonString())
        }
        if (attachment.deliveryDeadlineTimestamp <= dp.blockchain.lastBlock.timestamp) {
            throw BurstException.NotCurrentlyValidException("Delivery deadline has already expired: " + attachment.deliveryDeadlineTimestamp)
        }
    }

    override fun hasRecipient() = true

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DigitalGoodsPurchase::class.java)
    }
}
