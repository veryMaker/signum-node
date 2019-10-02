package brs.transaction.accountControl

import brs.*
import brs.util.toJsonString
import com.google.gson.JsonObject
import java.nio.ByteBuffer


class EffectiveBalanceLeasing(dp: DependencyProvider) : AccountControl(dp) {
    override val subtype = SUBTYPE_ACCOUNT_CONTROL_EFFECTIVE_BALANCE_LEASING
    override val description = "Effective Balance Leasing"

    override fun parseAttachment(buffer: ByteBuffer, transactionVersion: Byte): Attachment.AccountControlEffectiveBalanceLeasing {
        return Attachment.AccountControlEffectiveBalanceLeasing(dp, buffer, transactionVersion)
    }

    override fun parseAttachment(attachmentData: JsonObject): Attachment.AccountControlEffectiveBalanceLeasing {
        return Attachment.AccountControlEffectiveBalanceLeasing(dp, attachmentData)
    }

    override suspend fun applyAttachment(transaction: Transaction, senderAccount: Account, recipientAccount: Account?) {
        // TODO: check if anyone's used this or if it's even possible to use this, and eliminate it if possible
        // TODO harry1453: people have used this, remove in next fork
    }

    @Throws(BurstException.ValidationException::class)
    override fun validateAttachment(transaction: Transaction) {
        val attachment = transaction.attachment as Attachment.AccountControlEffectiveBalanceLeasing
        val recipientAccount = dp.accountService.getAccount(transaction.recipientId)
        if (transaction.senderId == transaction.recipientId || transaction.amountNQT != 0L || attachment.period < 1440) {
            throw BurstException.NotValidException("Invalid effective balance leasing: " + transaction.jsonObject.toJsonString() + " transaction " + transaction.stringId)
        }
        if (recipientAccount == null || recipientAccount!!.publicKey == null && transaction.stringId != "5081403377391821646") {
            throw BurstException.NotCurrentlyValidException("Invalid effective balance leasing: recipient account ${transaction.recipientId} not found or no public key published")
        }
    }

    override fun hasRecipient() = true
}