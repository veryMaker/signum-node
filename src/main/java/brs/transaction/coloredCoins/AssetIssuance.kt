package brs.transaction.coloredCoins

import brs.*
import brs.util.TextUtils
import brs.util.toJsonString
import com.google.gson.JsonObject
import java.nio.ByteBuffer

class AssetIssuance(dp: DependencyProvider) : ColoredCoins(dp) {
    override val subtype = SUBTYPE_COLORED_COINS_ASSET_ISSUANCE

    override val description = "Asset Issuance"

    public override fun getBaselineFee(height: Int) = BASELINE_ASSET_ISSUANCE_FEE

    override fun parseAttachment(
        buffer: ByteBuffer,
        transactionVersion: Byte
    ): Attachment.ColoredCoinsAssetIssuance {
        return Attachment.ColoredCoinsAssetIssuance(dp, buffer, transactionVersion)
    }

    override fun parseAttachment(attachmentData: JsonObject) = Attachment.ColoredCoinsAssetIssuance(dp, attachmentData)

    override suspend fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = true

    override suspend fun applyAttachment(
        transaction: Transaction,
        senderAccount: Account,
        recipientAccount: Account?
    ) {
        val attachment = transaction.attachment as Attachment.ColoredCoinsAssetIssuance
        val assetId = transaction.id
        dp.assetExchange.addAsset(transaction, attachment)
        dp.accountService.addToAssetAndUnconfirmedAssetBalanceQNT(
            senderAccount,
            assetId,
            attachment.quantityQNT
        )
    }

    override suspend fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) {
        // Nothing to undo
    }

    override suspend fun validateAttachment(transaction: Transaction) {
        val attachment = transaction.attachment as Attachment.ColoredCoinsAssetIssuance
        if (attachment.name!!.length < Constants.MIN_ASSET_NAME_LENGTH
            || attachment.name.length > Constants.MAX_ASSET_NAME_LENGTH
            || attachment.description.length > Constants.MAX_ASSET_DESCRIPTION_LENGTH
            || attachment.decimals < 0 || attachment.decimals > 8
            || attachment.quantityQNT <= 0
            || attachment.quantityQNT > Constants.MAX_ASSET_QUANTITY_QNT
        ) {
            throw BurstException.NotValidException("Invalid asset issuance: " + attachment.jsonObject.toJsonString())
        }
        if (!TextUtils.isInAlphabet(attachment.name)) {
            throw BurstException.NotValidException("Invalid asset name: " + attachment.name)
        }
    }

    override fun hasRecipient() = false
}
