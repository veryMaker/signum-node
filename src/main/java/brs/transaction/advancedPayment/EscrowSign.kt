package brs.transaction.advancedPayment

import brs.*
import brs.transactionduplicates.TransactionDuplicationKey
import brs.util.toUnsignedString
import com.google.gson.JsonObject
import java.nio.ByteBuffer

class EscrowSign(dp: DependencyProvider) : AdvancedPayment(dp) {
    override val subtype = SUBTYPE_ADVANCED_PAYMENT_ESCROW_SIGN
    override val description = "Escrow Sign"

    override fun parseAttachment(buffer: ByteBuffer, transactionVersion: Byte) = Attachment.AdvancedPaymentEscrowSign(dp, buffer, transactionVersion)

    override fun parseAttachment(attachmentData: JsonObject) = Attachment.AdvancedPaymentEscrowSign(dp, attachmentData)

    override suspend fun applyAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = true

    override suspend fun applyAttachment(transaction: Transaction, senderAccount: Account, recipientAccount: Account?) {
        val attachment = transaction.attachment as Attachment.AdvancedPaymentEscrowSign
        val escrow = dp.escrowService.getEscrowTransaction(attachment.escrowId)!!
        dp.escrowService.sign(senderAccount.id, attachment.decision!!, escrow)
    }

    override suspend fun undoAttachmentUnconfirmed(transaction: Transaction, senderAccount: Account) = Unit

    override fun getDuplicationKey(transaction: Transaction): TransactionDuplicationKey {
        val attachment = transaction.attachment as Attachment.AdvancedPaymentEscrowSign
        val uniqueString = attachment.escrowId!!.toUnsignedString() + ":" +
                transaction.senderId.toUnsignedString()
        return TransactionDuplicationKey(EscrowSign::class, uniqueString)
    }

    override fun validateAttachment(transaction: Transaction) {
        val attachment = transaction.attachment as Attachment.AdvancedPaymentEscrowSign
        if (transaction.amountNQT != 0L || transaction.feeNQT != Constants.ONE_BURST) {
            throw BurstException.NotValidException("Escrow signing must have amount 0 and fee of 1")
        }
        if (attachment.escrowId == null || attachment.decision == null) {
            throw BurstException.NotValidException("Escrow signing requires escrow id and decision set")
        }
        val escrow = dp.escrowService.getEscrowTransaction(attachment.escrowId)
            ?: throw BurstException.NotValidException("Escrow transaction not found")
        if (!dp.escrowService.isIdSigner(transaction.senderId, escrow) &&
            escrow.senderId != transaction.senderId &&
            escrow.recipientId != transaction.senderId
        ) {
            throw BurstException.NotValidException("Sender is not a participant in specified escrow")
        }
        if (escrow.senderId == transaction.senderId && attachment.decision != Escrow.DecisionType.RELEASE) {
            throw BurstException.NotValidException("Escrow sender can only release")
        }
        if (escrow.recipientId == transaction.senderId && attachment.decision != Escrow.DecisionType.REFUND) {
            throw BurstException.NotValidException("Escrow recipient can only refund")
        }
        if (!dp.escrowService.isEnabled) {
            throw BurstException.NotYetEnabledException("Escrow not yet enabled")
        }
    }

    override fun hasRecipient() = false
}
