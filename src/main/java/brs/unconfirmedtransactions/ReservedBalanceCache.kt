package brs.unconfirmedtransactions

import brs.Account
import brs.BurstException
import brs.BurstException.ValidationException
import brs.Transaction
import brs.db.store.AccountStore
import brs.util.convert.safeAdd
import brs.util.convert.safeSubtract
import brs.util.logging.safeInfo
import org.slf4j.LoggerFactory

internal class ReservedBalanceCache(private val accountStore: AccountStore) {
    private val reservedBalanceCache = mutableMapOf<Long, Long>()

    fun reserveBalanceAndPut(transaction: Transaction) {
        var senderAccount: Account? = null

        if (transaction.senderId != 0L) {
            senderAccount = accountStore.accountTable[accountStore.accountKeyFactory.newKey(transaction.senderId)]
        }

        val amountNQT = reservedBalanceCache.getOrDefault(transaction.senderId, 0L).safeAdd(transaction.type.calculateTotalAmountNQT(transaction))

        if (senderAccount == null) {
            logger.safeInfo { String.format("Transaction %d: Account %d does not exist and has no balance. Required funds: %d", transaction.id, transaction.senderId, amountNQT) }

            throw BurstException.NotCurrentlyValidException("Account unknown")
        } else if (amountNQT > senderAccount.unconfirmedBalanceNQT) {
            logger.safeInfo { String.format("Transaction %d: Account %d balance too low. You have  %d > %d Balance", transaction.id, transaction.senderId, amountNQT, senderAccount.unconfirmedBalanceNQT) }

            throw BurstException.NotCurrentlyValidException("Insufficient funds")
        }

        reservedBalanceCache[transaction.senderId] = amountNQT
    }

    fun refundBalance(transaction: Transaction) {
        val amountNQT = reservedBalanceCache.getOrDefault(transaction.senderId, 0L).safeSubtract(transaction.type.calculateTotalAmountNQT(transaction))

        if (amountNQT > 0) {
            reservedBalanceCache[transaction.senderId] = amountNQT
        } else {
            reservedBalanceCache.remove(transaction.senderId)
        }
    }

    fun rebuild(transactions: List<Transaction>): List<Transaction> {
        clear()

        val insufficientFundsTransactions = mutableListOf<Transaction>()

        for (t in transactions) {
            try {
                this.reserveBalanceAndPut(t)
            } catch (e: ValidationException) {
                insufficientFundsTransactions.add(t)
            }

        }

        return insufficientFundsTransactions
    }

    fun clear() {
        reservedBalanceCache.clear()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ReservedBalanceCache::class.java)
    }
}
