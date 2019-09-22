package brs.transaction.coloredCoins

import brs.*
import brs.util.toJsonString
import brs.util.toUnsignedString

abstract class OrderPlacement(dp: DependencyProvider) : ColoredCoins(dp) {
    override fun validateAttachment(transaction: Transaction) {
        val attachment = transaction.attachment as Attachment.ColoredCoinsOrderPlacement
        if (attachment.priceNQT <= 0 || attachment.priceNQT > Constants.MAX_BALANCE_NQT
            || attachment.assetId == 0L
        ) {
            throw BurstException.NotValidException("Invalid asset order placement: " + attachment.jsonObject.toJsonString())
        }
        val asset = dp.assetExchange.getAsset(attachment.assetId)
        if (attachment.quantityQNT <= 0 || asset != null && attachment.quantityQNT > asset.quantityQNT) {
            throw BurstException.NotValidException("Invalid asset order placement asset or quantity: " + attachment.jsonObject.toJsonString())
        }
        if (asset == null) {
            throw BurstException.NotCurrentlyValidException(
                "Asset " + attachment.assetId.toUnsignedString() +
                        " does not exist yet"
            )
        }
    }

    override fun hasRecipient() = false
}
