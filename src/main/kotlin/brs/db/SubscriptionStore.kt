package brs.db

import brs.entity.Subscription

interface SubscriptionStore {
    /**
     * TODO
     */
    val subscriptionDbKeyFactory: BurstKey.LongKeyFactory<Subscription>

    /**
     * TODO
     */
    val subscriptionTable: MutableBatchEntityTable<Subscription>

    /**
     * TODO
     */
    fun getSubscriptionsByParticipant(accountId: Long): Collection<Subscription>

    /**
     * TODO
     */
    fun getIdSubscriptions(accountId: Long): Collection<Subscription>

    /**
     * TODO
     */
    fun getSubscriptionsToId(accountId: Long): Collection<Subscription>

    /**
     * TODO
     */
    fun getUpdateSubscriptions(timestamp: Int): Collection<Subscription>
}
