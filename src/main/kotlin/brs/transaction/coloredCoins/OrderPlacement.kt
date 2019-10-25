package brs.transaction.coloredCoins

import brs.*
import brs.util.convert.toUnsignedString
import brs.util.toJsonString

abstract class OrderPlacement(dp: DependencyProvider) : ColoredCoins(dp) {
    override fun validateAttachment(transaction: Transaction) {
        val attachment = transaction.attachment as Attachment.ColoredCoinsOrderPlacement
        if (attachment.pricePlanck <= 0 || attachment.pricePlanck > Constants.MAX_BALANCE_PLANCK
            || attachment.assetId == 0L
        ) {
            throw BurstException.NotValidException("Invalid asset order placement: " + attachment.jsonObject.toJsonString())
        }
        val asset = dp.assetExchange.getAsset(attachment.assetId)
        if (attachment.quantity <= 0 || asset != null && attachment.quantity > asset.quantity) {
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
