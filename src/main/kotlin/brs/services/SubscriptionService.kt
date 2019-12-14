package brs.services

import brs.entity.Account
import brs.entity.Block
import brs.entity.Subscription

interface SubscriptionService {

    /**
     * TODO
     */
    fun getSubscription(id: Long?): Subscription?

    /**
     * TODO
     */
    fun getSubscriptionsByParticipant(accountId: Long): Collection<Subscription>

    /**
     * TODO
     */
    fun getSubscriptionsToId(accountId: Long): Collection<Subscription>

    /**
     * TODO
     */
    fun addSubscription(
        sender: Account,
        recipient: Account,
        id: Long,
        amountPlanck: Long,
        startTimestamp: Int,
        frequency: Int
    )

    /**
     * TODO
     */
    fun applyConfirmed(block: Block, blockchainHeight: Int)

    /**
     * TODO
     */
    fun removeSubscription(id: Long)

    /**
     * TODO
     */
    fun calculateFees(timestamp: Int): Long

    /**
     * TODO
     */
    fun clearRemovals()

    /**
     * TODO
     */
    fun addRemoval(id: Long)

    /**
     * TODO
     */
    fun applyUnconfirmed(timestamp: Int): Long
}
