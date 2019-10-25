package brs.transaction.coloredCoins

import brs.Account
import brs.Attachment
import brs.DependencyProvider
import brs.Transaction
import brs.util.logging.safeTrace
import com.google.gson.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

class AskOrderPlacement(dp: DependencyProvider) : OrderPlacement(dp) {
    override val subtype = SUBTYPE_COLORED_COINS_ASK_ORDER_PLACEMENT
    override val description = "Ask Order Placement"

    override fun parseAttachment(
        buffer: ByteBuffer,
        transactionVersion: Byte
    ): Attachment.ColoredCoinsAskOrderPlacement {
        return Attachment.ColoredCoinsAskOrderPlacement(dp, buffer, transactionVersion)
    }

    override fun parseAttachment(attachmentData: JsonObject) = Attachment.ColoredCoinsAskOrderPlacement(dp, attachmentData)

    override fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account): Boolean {
        logger.safeTrace { "TransactionType ASK_ORDER_PLACEMENT" }
        val attachment = transaction.attachment as Attachment.ColoredCoinsAskOrderPlacement
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
        val attachment = transaction.attachment as Attachment.ColoredCoinsAskOrderPlacement
        if (dp.assetExchange.getAsset(attachment.assetId) != null) {
            dp.assetExchange.addAskOrder(transaction, attachment)
        }
    }

    override fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) {
        val attachment = transaction.attachment as Attachment.ColoredCoinsAskOrderPlacement
        dp.accountService.addToUnconfirmedAssetBalanceQNT(
            senderAccount,
            attachment.assetId,
            attachment.quantityQNT
        )
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(AskOrderPlacement::class.java)
    }
}
