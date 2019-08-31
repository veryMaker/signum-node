package brs.unconfirmedtransactions

import brs.Account
import brs.BurstException
import brs.BurstException.ValidationException
import brs.Transaction
import brs.db.store.AccountStore
import brs.util.Convert
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.ArrayList
import java.util.HashMap

internal class ReservedBalanceCache(private val accountStore: AccountStore) {

    private val reservedBalanceCache: HashMap<Long, Long>

    init {

        this.reservedBalanceCache = HashMap()
    }

    @Throws(BurstException.ValidationException::class)
    fun reserveBalanceAndPut(transaction: Transaction) {
        var senderAccount: Account? = null

        if (transaction.senderId != 0L) {
            senderAccount = accountStore.accountTable.get(accountStore.accountKeyFactory.newKey(transaction.senderId))
        }

        val amountNQT = Convert.safeAdd(
                (reservedBalanceCache as java.util.Map<Long, Long>).getOrDefault(transaction.senderId, 0L),
                transaction.type!!.calculateTotalAmountNQT(transaction)!!
        )

        if (senderAccount == null) {
            if (LOGGER.isInfoEnabled) {
                LOGGER.info(String.format("Transaction %d: Account %d does not exist and has no balance. Required funds: %d", transaction.id, transaction.senderId, amountNQT))
            }

            throw BurstException.NotCurrentlyValidException("Account unknown")
        } else if (amountNQT > senderAccount.unconfirmedBalanceNQT) {
            if (LOGGER.isInfoEnabled) {
                LOGGER.info(String.format("Transaction %d: Account %d balance too low. You have  %d > %d Balance", transaction.id, transaction.senderId, amountNQT, senderAccount.unconfirmedBalanceNQT))
            }

            throw BurstException.NotCurrentlyValidException("Insufficient funds")
        }

        reservedBalanceCache[transaction.senderId] = amountNQT
    }

    fun refundBalance(transaction: Transaction) {
        val amountNQT = Convert.safeSubtract(
                (reservedBalanceCache as java.util.Map<Long, Long>).getOrDefault(transaction.senderId, 0L),
                transaction.type!!.calculateTotalAmountNQT(transaction)!!
        )

        if (amountNQT > 0) {
            reservedBalanceCache[transaction.senderId] = amountNQT
        } else {
            reservedBalanceCache.remove(transaction.senderId)
        }
    }

    fun rebuild(transactions: List<Transaction>): List<Transaction> {
        clear()

        val insufficientFundsTransactions = ArrayList<Transaction>()

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

        private val LOGGER = LoggerFactory.getLogger(ReservedBalanceCache::class.java)
    }

}
