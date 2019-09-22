package brs.transaction.messaging

import brs.*
import brs.fluxcapacitor.FluxValues
import brs.transaction.TransactionType
import brs.transactionduplicates.TransactionDuplicationKey
import brs.util.toUnsignedString
import com.google.gson.JsonObject
import java.nio.ByteBuffer
import java.util.*

class AliasBuy(dp: DependencyProvider) : Messaging(dp) {
    override val subtype = TransactionType.SUBTYPE_MESSAGING_ALIAS_BUY

    override val description = "Alias Buy"

    override fun parseAttachment(
        buffer: ByteBuffer,
        transactionVersion: Byte
    ): Attachment.MessagingAliasBuy {
        return Attachment.MessagingAliasBuy(dp, buffer, transactionVersion)
    }

    override fun parseAttachment(attachmentData: JsonObject) = Attachment.MessagingAliasBuy(dp, attachmentData)

    override fun applyAttachment(
        transaction: Transaction,
        senderAccount: Account,
        recipientAccount: Account?
    ) {
        val attachment = transaction.attachment as Attachment.MessagingAliasBuy
        val aliasName = attachment.aliasName
        dp.aliasService.changeOwner(transaction.senderId, aliasName, transaction.blockTimestamp)
    }

    override fun getDuplicationKey(transaction: Transaction): TransactionDuplicationKey {
        val attachment = transaction.attachment as Attachment.MessagingAliasBuy
        // not a bug, uniqueness is based on Messaging.ALIAS_ASSIGNMENT
        return TransactionDuplicationKey(AliasAssignment::class, attachment.aliasName.toLowerCase(Locale.ENGLISH))
    }

    override fun validateAttachment(transaction: Transaction) {
        if (!dp.fluxCapacitor.getValue(FluxValues.DIGITAL_GOODS_STORE, dp.blockchain.lastBlock.height)) {
            throw BurstException.NotYetEnabledException("Alias transfer not yet enabled at height " + dp.blockchain.lastBlock.height)
        }
        val attachment = transaction.attachment as Attachment.MessagingAliasBuy
        val aliasName = attachment.aliasName
        val alias = dp.aliasService.getAlias(aliasName)
        if (alias == null) {
            throw BurstException.NotCurrentlyValidException("Alias hasn't been registered yet: $aliasName")
        } else if (alias.accountId != transaction.recipientId) {
            throw BurstException.NotCurrentlyValidException("Alias is owned by account other than recipient: " + alias.accountId.toUnsignedString())
        }
        val offer = dp.aliasService.getOffer(alias)
            ?: throw BurstException.NotCurrentlyValidException("Alias is not for sale: $aliasName")
        if (transaction.amountNQT < offer.priceNQT) {
            val msg = ("Price is too low for: " + aliasName + " ("
                    + transaction.amountNQT + " < " + offer.priceNQT + ")")
            throw BurstException.NotCurrentlyValidException(msg)
        }
        if (offer.buyerId != 0L && offer.buyerId != transaction.senderId) {
            throw BurstException.NotCurrentlyValidException("Wrong buyer for " + aliasName + ": " + transaction.senderId.toUnsignedString() + " expected: " + offer.buyerId.toUnsignedString())
        }
    }

    override fun hasRecipient() = true
}
