package brs.transaction.messaging

import brs.*
import brs.util.convert.toBytes
import brs.util.toJsonString
import com.google.gson.JsonObject
import java.nio.ByteBuffer

class AccountInfo(dp: DependencyProvider) : Messaging(dp) {

    override val subtype = SUBTYPE_MESSAGING_ACCOUNT_INFO

    override val description = "Account Info"

    override fun parseAttachment(
        buffer: ByteBuffer,
        transactionVersion: Byte
    ): Attachment.MessagingAccountInfo {
        return Attachment.MessagingAccountInfo(dp, buffer, transactionVersion)
    }

    override fun parseAttachment(attachmentData: JsonObject) = Attachment.MessagingAccountInfo(dp, attachmentData)

    override fun validateAttachment(transaction: Transaction) {
        val attachment = transaction.attachment as Attachment.MessagingAccountInfo
        if (attachment.name.toBytes().size > Constants.MAX_ACCOUNT_NAME_LENGTH || attachment.description.toBytes().size > Constants.MAX_ACCOUNT_DESCRIPTION_LENGTH
        ) {
            throw BurstException.NotValidException("Invalid account info issuance: " + attachment.jsonObject.toJsonString())
        }
    }

    override suspend fun applyAttachment(
        transaction: Transaction,
        senderAccount: Account,
        recipientAccount: Account?
    ) {
        val attachment = transaction.attachment as Attachment.MessagingAccountInfo
        dp.accountService.setAccountInfo(senderAccount, attachment.name, attachment.description)
    }

    override fun hasRecipient() = false
}
