package brs.transaction.coloredCoins

import brs.*
import brs.util.convert.toUnsignedString
import brs.util.logging.safeTrace
import brs.util.toJsonString
import com.google.gson.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

class AssetTransfer(dp: DependencyProvider) : ColoredCoins(dp) {
    override val subtype = SUBTYPE_COLORED_COINS_ASSET_TRANSFER
    override val description = "Asset Transfer"

    override fun parseAttachment(
        buffer: ByteBuffer,
        transactionVersion: Byte
    ): Attachment.ColoredCoinsAssetTransfer {
        return Attachment.ColoredCoinsAssetTransfer(dp, buffer, transactionVersion)
    }

    override fun parseAttachment(attachmentData: JsonObject) = Attachment.ColoredCoinsAssetTransfer(dp, attachmentData)

    override fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account): Boolean {
        logger.safeTrace { "TransactionType ASSET_TRANSFER" }
        val attachment = transaction.attachment as Attachment.ColoredCoinsAssetTransfer
        val unconfirmedAssetBalance = dp.accountService.getUnconfirmedAssetBalanceQNT(senderAccount, attachment.assetId)
        if (unconfirmedAssetBalance >= 0 && unconfirmedAssetBalance >= attachment.quantityQNT) {
            dp.accountService.addToUnconfirmedAssetBalanceQNT(
                senderAccount,
                attachment.assetId,
                -attachment.quantityQNT
            )
            return true
        }
        return false
    }

    override fun applyAttachment(
        transaction: Transaction,
        senderAccount: Account,
        recipientAccount: Account?
    ) {
        val attachment = transaction.attachment as Attachment.ColoredCoinsAssetTransfer
        dp.accountService.addToAssetBalanceQNT(senderAccount, attachment.assetId, -attachment.quantityQNT)
        dp.accountService.addToAssetAndUnconfirmedAssetBalanceQNT(
            recipientAccount!!,
            attachment.assetId,
            attachment.quantityQNT
        )
        dp.assetExchange.addAssetTransfer(transaction, attachment)
    }

    override fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) {
        val attachment = transaction.attachment as Attachment.ColoredCoinsAssetTransfer
        dp.accountService.addToUnconfirmedAssetBalanceQNT(
            senderAccount,
            attachment.assetId,
            attachment.quantityQNT
        )
    }

    override fun validateAttachment(transaction: Transaction) {
        val attachment = transaction.attachment as Attachment.ColoredCoinsAssetTransfer
        if (transaction.amountNQT != 0L
            || attachment.comment != null && attachment.comment.length > Constants.MAX_ASSET_TRANSFER_COMMENT_LENGTH
            || attachment.assetId == 0L
        ) {
            throw BurstException.NotValidException("Invalid asset transfer amount or comment: " + attachment.jsonObject.toJsonString())
        }
        if (transaction.version > 0 && attachment.comment != null) {
            throw BurstException.NotValidException("Asset transfer comments no longer allowed, use message " + "or encrypted message appendix instead")
        }
        val asset = dp.assetExchange.getAsset(attachment.assetId)
        if (attachment.quantityQNT <= 0 || asset != null && attachment.quantityQNT > asset.quantityQNT) {
            throw BurstException.NotValidException("Invalid asset transfer asset or quantity: " + attachment.jsonObject.toJsonString())
        }
        if (asset == null) {
            throw BurstException.NotCurrentlyValidException(
                "Asset " + attachment.assetId.toUnsignedString() +
                        " does not exist yet"
            )
        }
    }

    override fun hasRecipient() = true

    companion object {
        val logger: Logger = LoggerFactory.getLogger(AssetTransfer::class.java)
    }
}
