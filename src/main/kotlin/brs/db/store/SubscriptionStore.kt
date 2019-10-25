package brs.db.store

import brs.entity.Subscription
import brs.db.BurstKey
import brs.db.VersionedEntityTable

interface SubscriptionStore {
    val subscriptionDbKeyFactory: BurstKey.LongKeyFactory<Subscription>

    val subscriptionTable: VersionedEntityTable<Subscription>

    fun getSubscriptionsByParticipant(accountId: Long?): Collection<Subscription>

    fun getIdSubscriptions(accountId: Long?): Collection<Subscription>

    fun getSubscriptionsToId(accountId: Long?): Collection<Subscription>

    fun getUpdateSubscriptions(timestamp: Int): Collection<Subscription>
}
