package brs.services.impl

import brs.BurstException
import brs.DependencyProvider
import brs.Transaction
import brs.services.TransactionService

class TransactionServiceImpl(private val dp: DependencyProvider) : TransactionService {

    override suspend fun verifyPublicKey(transaction: Transaction): Boolean {
        val account = dp.accountService.getAccount(transaction.senderId) ?: return false
        return if (transaction.signature == null) {
            false
        } else account.setOrVerify(dp, transaction.senderPublicKey, transaction.height)
    }

    override suspend fun validate(transaction: Transaction) {
        for (appendage in transaction.appendages) {
            appendage.validate(transaction)
        }
        val minimumFeeNQT = transaction.type.minimumFeeNQT(dp.blockchain.height, transaction.appendagesSize)
        if (transaction.feeNQT < minimumFeeNQT) {
            throw BurstException.NotCurrentlyValidException(String.format("Transaction fee %d less than minimum fee %d at height %d",
                    transaction.feeNQT, minimumFeeNQT, dp.blockchain.height))
        }
    }

    override suspend fun applyUnconfirmed(transaction: Transaction): Boolean {
        val senderAccount = dp.accountService.getAccount(transaction.senderId)
        return senderAccount != null && transaction.type.applyUnconfirmed(transaction, senderAccount)
    }

    override suspend fun apply(transaction: Transaction) {
        val senderAccount = dp.accountService.getAccount(transaction.senderId)!!
        senderAccount.apply(dp, transaction.senderPublicKey, transaction.height)
        val recipientAccount = dp.accountService.getOrAddAccount(transaction.recipientId)
        for (appendage in transaction.appendages) {
            appendage.apply(transaction, senderAccount, recipientAccount)
        }
    }

    override suspend fun undoUnconfirmed(transaction: Transaction) {
        val senderAccount = dp.accountService.getAccount(transaction.senderId)!!
        transaction.type.undoUnconfirmed(transaction, senderAccount)
    }

}
