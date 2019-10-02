package brs.transaction.digitalGoods

import brs.*
import brs.transactionduplicates.TransactionDuplicationKey
import brs.util.toJsonString
import brs.util.toUnsignedString
import com.google.gson.JsonObject
import java.nio.ByteBuffer

class DigitalGoodsQuantityChange(dp: DependencyProvider) : DigitalGoods(dp) {
    override val subtype = SUBTYPE_DIGITAL_GOODS_QUANTITY_CHANGE
    override val description = "Quantity Change"
    override fun parseAttachment(buffer: ByteBuffer, transactionVersion: Byte) = Attachment.DigitalGoodsQuantityChange(dp, buffer, transactionVersion)
    override fun parseAttachment(attachmentData: JsonObject) = Attachment.DigitalGoodsQuantityChange(dp, attachmentData)

    override suspend fun applyAttachment(transaction: Transaction, senderAccount: Account, recipientAccount: Account?) {
        val attachment = transaction.attachment as Attachment.DigitalGoodsQuantityChange
        dp.digitalGoodsStoreService.changeQuantity(attachment.goodsId, attachment.deltaQuantity, false)
    }

    override fun doValidateAttachment(transaction: Transaction) {
        val attachment = transaction.attachment as Attachment.DigitalGoodsQuantityChange
        val goods = dp.digitalGoodsStoreService.getGoods(attachment.goodsId)
        if (attachment.deltaQuantity < -Constants.MAX_DGS_LISTING_QUANTITY
            || attachment.deltaQuantity > Constants.MAX_DGS_LISTING_QUANTITY
            || goods != null && transaction.senderId != goods.sellerId
        ) {
            throw BurstException.NotValidException("Invalid digital goods quantity change: " + attachment.jsonObject.toJsonString())
        }
        if (goods == null || goods.isDelisted) {
            throw BurstException.NotCurrentlyValidException(
                "Goods " + attachment.goodsId.toUnsignedString() +
                        "not yet listed or already delisted"
            )
        }
    }

    override fun getDuplicationKey(transaction: Transaction): TransactionDuplicationKey {
        val attachment = transaction.attachment as Attachment.DigitalGoodsQuantityChange
        // not a bug, uniqueness is based on DigitalGoods.DELISTING
        return TransactionDuplicationKey(DigitalGoodsDelisting::class, attachment.goodsId.toUnsignedString())
    }

    override fun hasRecipient() = false
}
