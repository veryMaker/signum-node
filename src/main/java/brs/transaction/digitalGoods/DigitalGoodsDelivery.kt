package brs.transaction.digitalGoods

import brs.*
import brs.transactionduplicates.TransactionDuplicationKey
import brs.util.Convert
import brs.util.toJsonString
import brs.util.toUnsignedString
import com.google.gson.JsonObject
import java.nio.ByteBuffer

class DigitalGoodsDelivery(dp: DependencyProvider) : DigitalGoods(dp) {
    override val subtype = SUBTYPE_DIGITAL_GOODS_DELIVERY
    override val description = "Delivery"
    override fun parseAttachment(buffer: ByteBuffer, transactionVersion: Byte) = Attachment.DigitalGoodsDelivery(dp, buffer, transactionVersion)
    override fun parseAttachment(attachmentData: JsonObject) = Attachment.DigitalGoodsDelivery(dp, attachmentData)

    override suspend fun applyAttachment(transaction: Transaction, senderAccount: Account, recipientAccount: Account?) {
        val attachment = transaction.attachment as Attachment.DigitalGoodsDelivery
        dp.digitalGoodsStoreService.deliver(transaction, attachment)
    }

    override fun doValidateAttachment(transaction: Transaction) {
        val attachment = transaction.attachment as Attachment.DigitalGoodsDelivery
        val purchase = dp.digitalGoodsStoreService.getPendingPurchase(attachment.purchaseId)
        if (attachment.goods.data.size > Constants.MAX_DGS_GOODS_LENGTH
            || attachment.goods.data.isEmpty()
            || attachment.goods.nonce.size != 32
            || attachment.discountNQT < 0 || attachment.discountNQT > Constants.MAX_BALANCE_NQT
            || purchase != null && (purchase.buyerId != transaction.recipientId
                    || transaction.senderId != purchase.sellerId
                    || attachment.discountNQT > Convert.safeMultiply(
                purchase.priceNQT,
                purchase.quantity.toLong()
            ))
        ) {
            throw BurstException.NotValidException("Invalid digital goods delivery: " + attachment.jsonObject.toJsonString())
        }
        if (purchase == null || purchase.encryptedGoods != null) {
            throw BurstException.NotCurrentlyValidException("Purchase does not exist yet, or already delivered: " + attachment.jsonObject.toJsonString())
        }
    }

    override fun getDuplicationKey(transaction: Transaction): TransactionDuplicationKey {
        val attachment = transaction.attachment as Attachment.DigitalGoodsDelivery
        return TransactionDuplicationKey(DigitalGoodsDelivery::class, attachment.purchaseId.toUnsignedString())
    }

    override fun hasRecipient() = true
}
