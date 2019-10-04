package brs.transaction.advancedPayment

import brs.*
import brs.transaction.TransactionType
import brs.transactionduplicates.TransactionDuplicationKey
import brs.util.convert.safeAdd
import brs.util.convert.safeMultiply
import brs.util.logging.safeTrace
import com.google.gson.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer

class EscrowCreation(dp: DependencyProvider) : AdvancedPayment(dp) {
    override val subtype = TransactionType.SUBTYPE_ADVANCED_PAYMENT_ESCROW_CREATION
    override val description = "Escrow Creation"

    override fun parseAttachment(
        buffer: ByteBuffer,
        transactionVersion: Byte
    ): Attachment.AdvancedPaymentEscrowCreation {
        return Attachment.AdvancedPaymentEscrowCreation(dp, buffer, transactionVersion)
    }

    override fun parseAttachment(attachmentData: JsonObject) = Attachment.AdvancedPaymentEscrowCreation(dp, attachmentData)

    override suspend fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account): Boolean {
        logger.safeTrace { "TransactionType ESCROW_CREATION" }
        val totalAmountNQT = calculateAttachmentTotalAmountNQT(transaction)
        if (senderAccount.unconfirmedBalanceNQT < totalAmountNQT) {
            return false
        }
        dp.accountService.addToUnconfirmedBalanceNQT(senderAccount, -totalAmountNQT)
        return true
    }

    public override fun calculateAttachmentTotalAmountNQT(transaction: Transaction): Long {
        val attachment = transaction.attachment as Attachment.AdvancedPaymentEscrowCreation
        return attachment.amountNQT.safeAdd(attachment.totalSigners.toLong().safeMultiply(Constants.ONE_BURST))
    }

    override suspend fun applyAttachment(
        transaction: Transaction,
        senderAccount: Account,
        recipientAccount: Account?
    ) {
        val attachment = transaction.attachment as Attachment.AdvancedPaymentEscrowCreation
        val totalAmountNQT = calculateAttachmentTotalAmountNQT(transaction)
        dp.accountService.addToBalanceNQT(senderAccount, -totalAmountNQT)
        val signers = attachment.getSigners()
        signers.forEach { signer ->
            dp.accountService.addToBalanceAndUnconfirmedBalanceNQT(
                dp.accountService.getOrAddAccount(
                    signer
                ), Constants.ONE_BURST
            )
        }
        dp.escrowService.addEscrowTransaction(
            senderAccount,
            recipientAccount!!,
            transaction.id,
            attachment.amountNQT,
            attachment.getRequiredSigners(),
            attachment.getSigners(),
            transaction.timestamp + attachment.deadline,
            attachment.deadlineAction!!
        )
    }

    override suspend fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) {
        dp.accountService.addToUnconfirmedBalanceNQT(
            senderAccount,
            calculateAttachmentTotalAmountNQT(transaction)
        )
    }

    override fun getDuplicationKey(transaction: Transaction): TransactionDuplicationKey {
        return TransactionDuplicationKey.IS_NEVER_DUPLICATE
    }

    override fun validateAttachment(transaction: Transaction) {
        val attachment = transaction.attachment as Attachment.AdvancedPaymentEscrowCreation
        var totalAmountNQT: Long? = attachment.amountNQT!!.safeAdd(transaction.feeNQT)
        if (transaction.senderId == transaction.recipientId) {
            throw BurstException.NotValidException("Escrow must have different sender and recipient")
        }
        totalAmountNQT = totalAmountNQT!!.safeAdd(attachment.totalSigners * Constants.ONE_BURST)
        if (transaction.amountNQT != 0L) {
            throw BurstException.NotValidException("Transaction sent amount must be 0 for escrow")
        }
        if (totalAmountNQT < 0L || totalAmountNQT > Constants.MAX_BALANCE_NQT) {
            throw BurstException.NotValidException("Invalid escrow creation amount")
        }
        if (transaction.feeNQT < Constants.ONE_BURST) {
            throw BurstException.NotValidException("Escrow transaction must have a fee at least 1 burst")
        }
        if (attachment.getRequiredSigners() < 1 || attachment.getRequiredSigners() > 10) {
            throw BurstException.NotValidException("Escrow required signers much be 1 - 10")
        }
        if (attachment.getRequiredSigners() > attachment.totalSigners) {
            throw BurstException.NotValidException("Cannot have more required than signers on escrow")
        }
        if (attachment.totalSigners < 1 || attachment.totalSigners > 10) {
            throw BurstException.NotValidException("Escrow transaction requires 1 - 10 signers")
        }
        if (attachment.deadline < 1 || attachment.deadline > 7776000) { // max deadline 3 months
            throw BurstException.NotValidException("Escrow deadline must be 1 - 7776000 seconds")
        }
        if (attachment.deadlineAction == null || attachment.deadlineAction == Escrow.DecisionType.UNDECIDED) {
            throw BurstException.NotValidException("Invalid deadline action for escrow")
        }
        if (attachment.getSigners().contains(transaction.senderId) || attachment.getSigners().contains(
                transaction.recipientId
            )
        ) {
            throw BurstException.NotValidException("Escrow sender and recipient cannot be signers")
        }
        if (!dp.escrowService.isEnabled) {
            throw BurstException.NotYetEnabledException("Escrow not yet enabled")
        }
    }

    override fun hasRecipient() = true

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(EscrowCreation::class.java)
    }
}
