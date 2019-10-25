package brs.services

import brs.Account
import brs.Block
import brs.Subscription

interface SubscriptionService {
    fun isEnabled(): Boolean

    fun getSubscription(id: Long?): Subscription?

    fun getSubscriptionsByParticipant(accountId: Long?): Collection<Subscription>

    fun getSubscriptionsToId(accountId: Long?): Collection<Subscription>

    fun addSubscription(sender: Account, recipient: Account, id: Long, amountPlanck: Long, startTimestamp: Int, frequency: Int)

    fun applyConfirmed(block: Block, blockchainHeight: Int)

    fun removeSubscription(id: Long)

    fun calculateFees(timestamp: Int): Long

    fun clearRemovals()

    fun addRemoval(id: Long)

    fun applyUnconfirmed(timestamp: Int): Long
}
