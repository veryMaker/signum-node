package brs.services.impl

import brs.util.BurstException
import brs.DependencyProvider
import brs.entity.Transaction
import brs.services.TransactionService

class TransactionServiceImpl(private val dp: DependencyProvider) : TransactionService {

    override fun verifyPublicKey(transaction: Transaction): Boolean {
        val account = dp.accountService.getAccount(transaction.senderId) ?: return false
        return if (transaction.signature == null) {
            false
        } else account.setOrVerify(dp, transaction.senderPublicKey, transaction.height)
    }

    override fun validate(transaction: Transaction) {
        for (appendage in transaction.appendages) {
            appendage.validate(transaction)
        }
        val minimumFeePlanck = transaction.type.minimumFeePlanck(dp.blockchainService.height, transaction.appendagesSize)
        if (transaction.feePlanck < minimumFeePlanck) {
            throw BurstException.NotCurrentlyValidException(String.format("Transaction fee %d less than minimum fee %d at height %d",
                    transaction.feePlanck, minimumFeePlanck, dp.blockchainService.height))
        }
    }

    override fun applyUnconfirmed(transaction: Transaction): Boolean {
        val senderAccount = dp.accountService.getAccount(transaction.senderId)
        return senderAccount != null && transaction.type.applyUnconfirmed(transaction, senderAccount)
    }

    override fun apply(transaction: Transaction) {
        val senderAccount = dp.accountService.getAccount(transaction.senderId)!!
        senderAccount.apply(dp, transaction.senderPublicKey, transaction.height)
        val recipientAccount = dp.accountService.getOrAddAccount(transaction.recipientId)
        for (appendage in transaction.appendages) {
            appendage.apply(transaction, senderAccount, recipientAccount)
        }
    }

    override fun undoUnconfirmed(transaction: Transaction) {
        val senderAccount = dp.accountService.getAccount(transaction.senderId)!!
        transaction.type.undoUnconfirmed(transaction, senderAccount)
    }

}
