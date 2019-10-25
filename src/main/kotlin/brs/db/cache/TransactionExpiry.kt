package brs.db.cache

import brs.entity.Transaction
import brs.util.Time
import org.ehcache.expiry.ExpiryPolicy

import java.time.Duration
import java.util.function.Supplier

internal class TransactionExpiry : ExpiryPolicy<Long, Transaction> {
    override fun getExpiryForCreation(key: Long?, value: Transaction): Duration {
        return Duration.ofSeconds(value.expiration.toLong() - time.time)
    }

    override fun getExpiryForAccess(key: Long?, value: Supplier<out Transaction>): Duration {
        return Duration.ofSeconds(value.get().expiration.toLong() - time.time)
    }

    override fun getExpiryForUpdate(key: Long?, oldValue: Supplier<out Transaction>, newValue: Transaction): Duration {
        return Duration.ofSeconds(newValue.expiration.toLong() - time.time)
    }

    companion object {
        private val time = Time.EpochTime()
    }
}
