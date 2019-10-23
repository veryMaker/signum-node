package brs.services

import brs.Account
import brs.Block
import brs.Subscription

interface SubscriptionService {
    suspend fun isEnabled(): Boolean

    suspend fun getSubscription(id: Long?): Subscription?

    suspend fun getSubscriptionsByParticipant(accountId: Long?): Collection<Subscription>

    suspend fun getSubscriptionsToId(accountId: Long?): Collection<Subscription>

    suspend fun addSubscription(sender: Account, recipient: Account, id: Long, amountNQT: Long, startTimestamp: Int, frequency: Int)

    suspend fun applyConfirmed(block: Block, blockchainHeight: Int)

    suspend fun removeSubscription(id: Long)

    suspend fun calculateFees(timestamp: Int): Long

    fun clearRemovals()

    fun addRemoval(id: Long)

    suspend fun applyUnconfirmed(timestamp: Int): Long
}
