package brs.transaction.automatedTransactions

import brs.*
import brs.at.AT
import brs.at.AtException
import brs.fluxcapacitor.FluxValues
import brs.util.toUnsignedString
import com.google.gson.JsonObject
import java.nio.ByteBuffer
import java.util.*

class AutomatedTransactionCreation(dp: DependencyProvider) : AutomatedTransactions(dp) {
    override val subtype = SUBTYPE_AT_CREATION
    override val description = "AT Creation"

    override fun parseAttachment(buffer: ByteBuffer, transactionVersion: Byte) = Attachment.AutomatedTransactionsCreation(dp, buffer, transactionVersion)

    override fun parseAttachment(attachmentData: JsonObject) = Attachment.AutomatedTransactionsCreation(dp, attachmentData)

    override fun doValidateAttachment(transaction: Transaction) {
        if (!dp.fluxCapacitor.getValue(
                FluxValues.AUTOMATED_TRANSACTION_BLOCK,
                dp.blockchain.lastBlock.height
            )
        ) {
            throw BurstException.NotYetEnabledException("Automated Transactions not yet enabled at height " + dp.blockchain.lastBlock.height)
        }
        if (transaction.signature != null && dp.accountService.getAccount(transaction.id) != null) {
            val existingAccount = dp.accountService.getAccount(transaction.id)
                ?: throw BurstException.NotValidException("Account with transaction's ID does not exist")
            if (existingAccount.publicKey != null && !Arrays.equals(
                    existingAccount.publicKey,
                    ByteArray(32)
                )
            )
                throw BurstException.NotValidException("Account with id already exists")
        }
        val attachment = transaction.attachment as Attachment.AutomatedTransactionsCreation
        val totalPages: Long
        try {
            totalPages =
                dp.atController.checkCreationBytes(attachment.creationBytes, dp.blockchain.height).toLong()
        } catch (e: AtException) {
            throw BurstException.NotCurrentlyValidException("Invalid AT creation bytes", e)
        }

        val requiredFee = totalPages * dp.atConstants.costPerPage(transaction.height)
        if (transaction.feeNQT < requiredFee) {
            throw BurstException.NotValidException("Insufficient fee for AT creation. Minimum: " + (requiredFee / Constants.ONE_BURST).toUnsignedString())
        }
        if (dp.fluxCapacitor.getValue(FluxValues.AT_FIX_BLOCK_3)) {
            if (attachment.name!!.length > Constants.MAX_AUTOMATED_TRANSACTION_NAME_LENGTH) {
                throw BurstException.NotValidException("Name of automated transaction over size limit")
            }
            if (attachment.description!!.length > Constants.MAX_AUTOMATED_TRANSACTION_DESCRIPTION_LENGTH) {
                throw BurstException.NotValidException("Description of automated transaction over size limit")
            }
        }
    }

    override fun applyAttachment(transaction: Transaction, senderAccount: Account, recipientAccount: Account?) {
        val attachment = transaction.attachment as Attachment.AutomatedTransactionsCreation
        AT.addAT(
            dp,
            transaction.id,
            transaction.senderId,
            attachment.name.orEmpty(),
            attachment.description.orEmpty(),
            attachment.creationBytes,
            transaction.height
        )
    }

    override fun hasRecipient() = false
}
