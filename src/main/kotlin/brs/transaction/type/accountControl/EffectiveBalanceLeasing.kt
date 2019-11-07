package brs.transaction.type.accountControl

import brs.entity.Account
import brs.entity.DependencyProvider
import brs.entity.Transaction
import brs.transaction.appendix.Attachment
import brs.util.BurstException
import brs.util.json.toJsonString
import com.google.gson.JsonObject
import java.nio.ByteBuffer


class EffectiveBalanceLeasing(dp: DependencyProvider) : AccountControl(dp) {
    override val subtype = SUBTYPE_ACCOUNT_CONTROL_EFFECTIVE_BALANCE_LEASING
    override val description = "Effective Balance Leasing"

    override fun parseAttachment(
        buffer: ByteBuffer,
        transactionVersion: Byte
    ): Attachment.AccountControlEffectiveBalanceLeasing {
        return Attachment.AccountControlEffectiveBalanceLeasing(dp, buffer, transactionVersion)
    }

    override fun parseAttachment(attachmentData: JsonObject): Attachment.AccountControlEffectiveBalanceLeasing {
        return Attachment.AccountControlEffectiveBalanceLeasing(dp, attachmentData)
    }

    override fun applyAttachment(transaction: Transaction, senderAccount: Account, recipientAccount: Account) {
        // TODO harry1453: Remove in next fork
    }

    @Throws(BurstException.ValidationException::class)
    override fun validateAttachment(transaction: Transaction) {
        val attachment = transaction.attachment as Attachment.AccountControlEffectiveBalanceLeasing
        val recipientAccount = dp.accountService.getAccount(transaction.recipientId)
        if (transaction.senderId == transaction.recipientId || transaction.amountPlanck != 0L || attachment.period < 1440) {
            throw BurstException.NotValidException("Invalid effective balance leasing: " + transaction.toJsonObject().toJsonString() + " transaction " + transaction.stringId)
        }
        if (recipientAccount?.publicKey == null) {
            throw BurstException.NotCurrentlyValidException("Invalid effective balance leasing: recipient account ${transaction.recipientId} not found or no public key published")
        }
    }

    override fun hasRecipient() = true
}