package brs.transaction.messaging

import brs.*
import brs.transactionduplicates.TransactionDuplicationKey
import brs.util.TextUtils
import brs.util.convert.toBytes
import brs.util.toJsonString
import com.google.gson.JsonObject
import java.nio.ByteBuffer
import java.util.*

class AliasAssignment(dp: DependencyProvider) : Messaging(dp) {
    override val subtype = SUBTYPE_MESSAGING_ALIAS_ASSIGNMENT

    override val description = "Alias Assignment"

    override fun parseAttachment(
        buffer: ByteBuffer,
        transactionVersion: Byte
    ): Attachment.MessagingAliasAssignment {
        return Attachment.MessagingAliasAssignment(dp, buffer, transactionVersion)
    }

    override fun parseAttachment(attachmentData: JsonObject) = Attachment.MessagingAliasAssignment(dp, attachmentData)

    override suspend fun applyAttachment(
        transaction: Transaction,
        senderAccount: Account,
        recipientAccount: Account?
    ) {
        val attachment = transaction.attachment as Attachment.MessagingAliasAssignment
        dp.aliasService.addOrUpdateAlias(transaction, attachment)
    }

    override fun getDuplicationKey(transaction: Transaction): TransactionDuplicationKey {
        val attachment = transaction.attachment as Attachment.MessagingAliasAssignment
        return TransactionDuplicationKey(AliasAssignment::class, attachment.aliasName.toLowerCase(Locale.ENGLISH))
    }

    override fun validateAttachment(transaction: Transaction) {
        val attachment = transaction.attachment as Attachment.MessagingAliasAssignment
        if (attachment.aliasName.isEmpty()
            || attachment.aliasName.toBytes().size > Constants.MAX_ALIAS_LENGTH
            || attachment.aliasURI.length > Constants.MAX_ALIAS_URI_LENGTH
        ) {
            throw BurstException.NotValidException("Invalid alias assignment: " + attachment.jsonObject.toJsonString())
        }
        if (!TextUtils.isInAlphabet(attachment.aliasName)) {
            throw BurstException.NotValidException("Invalid alias name: " + attachment.aliasName)
        }
        val alias = dp.aliasService.getAlias(attachment.aliasName)
        if (alias != null && alias.accountId != transaction.senderId) {
            throw BurstException.NotCurrentlyValidException("Alias already owned by another account: " + attachment.aliasName)
        }
    }

    override fun hasRecipient() = false
}
