package brs.services

import brs.Account
import brs.Block
import brs.Subscription

interface SubscriptionService {
    val isEnabled: Boolean

    fun getSubscription(id: Long?): Subscription?

    fun getSubscriptionsByParticipant(accountId: Long?): Collection<Subscription>

    fun getSubscriptionsToId(accountId: Long?): Collection<Subscription>

    fun addSubscription(sender: Account, recipient: Account, id: Long, amountNQT: Long, startTimestamp: Int, frequency: Int)

    suspend fun applyConfirmed(block: Block, blockchainHeight: Int)

    fun removeSubscription(id: Long)

    suspend fun calculateFees(timestamp: Int): Long

    fun clearRemovals()

    fun addRemoval(id: Long)

    suspend fun applyUnconfirmed(timestamp: Int): Long
}
