package brs.transaction.unconfirmed

import brs.entity.Account
import brs.util.BurstException
import brs.util.BurstException.ValidationException
import brs.entity.Transaction
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

        val amountPlanck = reservedBalanceCache.getOrDefault(transaction.senderId, 0L).safeAdd(transaction.type.calculateTotalAmountPlanck(transaction))

        if (senderAccount == null) {
            logger.safeInfo { String.format("Transaction %d: Account %d does not exist and has no balance. Required funds: %d", transaction.id, transaction.senderId, amountPlanck) }

            throw BurstException.NotCurrentlyValidException("Account unknown")
        } else if (amountPlanck > senderAccount.unconfirmedBalancePlanck) {
            logger.safeInfo { String.format("Transaction %d: Account %d balance too low. You have  %d > %d Balance", transaction.id, transaction.senderId, amountPlanck, senderAccount.unconfirmedBalancePlanck) }

            throw BurstException.NotCurrentlyValidException("Insufficient funds")
        }

        reservedBalanceCache[transaction.senderId] = amountPlanck
    }

    fun refundBalance(transaction: Transaction) {
        val amountPlanck = reservedBalanceCache.getOrDefault(transaction.senderId, 0L).safeSubtract(transaction.type.calculateTotalAmountPlanck(transaction))

        if (amountPlanck > 0) {
            reservedBalanceCache[transaction.senderId] = amountPlanck
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
