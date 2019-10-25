package brs.transaction.messaging

import brs.*
import brs.fluxcapacitor.FluxValues
import brs.transactionduplicates.TransactionDuplicationKey
import brs.util.toJsonString
import com.google.gson.JsonObject
import java.nio.ByteBuffer
import java.util.*

class AliasSell(dp: DependencyProvider) : Messaging(dp) {

    override val subtype = SUBTYPE_MESSAGING_ALIAS_SELL

    override val description = "Alias Sell"

    override fun parseAttachment(
        buffer: ByteBuffer,
        transactionVersion: Byte
    ): Attachment.MessagingAliasSell {
        return Attachment.MessagingAliasSell(dp, buffer, transactionVersion)
    }

    override fun parseAttachment(attachmentData: JsonObject) = Attachment.MessagingAliasSell(dp, attachmentData)

    override fun applyAttachment(
        transaction: Transaction,
        senderAccount: Account,
        recipientAccount: Account?
    ) {
        val attachment = transaction.attachment as Attachment.MessagingAliasSell
        dp.aliasService.sellAlias(transaction, attachment)
    }

    override fun getDuplicationKey(transaction: Transaction): TransactionDuplicationKey {
        val attachment = transaction.attachment as Attachment.MessagingAliasSell
        // not a bug, uniqueness is based on Messaging.ALIAS_ASSIGNMENT
        return TransactionDuplicationKey(AliasAssignment::class, attachment.aliasName.toLowerCase(Locale.ENGLISH))
    }

    override fun validateAttachment(transaction: Transaction) {
        if (!dp.fluxCapacitor.getValue(FluxValues.DIGITAL_GOODS_STORE, dp.blockchain.lastBlock.height)) {
            throw BurstException.NotYetEnabledException("Alias transfer not yet enabled at height " + dp.blockchain.lastBlock.height)
        }
        if (transaction.amountNQT != 0L) {
            throw BurstException.NotValidException("Invalid sell alias transaction: " + transaction.toJsonObject().toJsonString())
        }
        val attachment = transaction.attachment as Attachment.MessagingAliasSell
        val aliasName = attachment.aliasName
        if (aliasName.isEmpty()) {
            throw BurstException.NotValidException("Missing alias name")
        }
        val priceNQT = attachment.priceNQT
        if (priceNQT < 0 || priceNQT > Constants.MAX_BALANCE_NQT) {
            throw BurstException.NotValidException("Invalid alias sell price: $priceNQT")
        }
        if (priceNQT == 0L) {
            if (Genesis.CREATOR_ID == transaction.recipientId) {
                throw BurstException.NotValidException("Transferring aliases to Genesis account not allowed")
            } else if (transaction.recipientId == 0L) {
                throw BurstException.NotValidException("Missing alias transfer recipient")
            }
        }
        val alias = dp.aliasService.getAlias(aliasName)
        if (alias == null) {
            throw BurstException.NotCurrentlyValidException("Alias hasn't been registered yet: $aliasName")
        } else if (alias.accountId != transaction.senderId) {
            throw BurstException.NotCurrentlyValidException("Alias doesn't belong to sender: $aliasName")
        }
    }

    override fun hasRecipient() = true
}
