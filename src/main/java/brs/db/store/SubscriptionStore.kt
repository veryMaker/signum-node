package brs.db.store

import brs.Subscription
import brs.db.BurstKey
import brs.db.VersionedEntityTable

interface SubscriptionStore {
    val subscriptionDbKeyFactory: BurstKey.LongKeyFactory<Subscription>

    val subscriptionTable: VersionedEntityTable<Subscription>

    suspend fun getSubscriptionsByParticipant(accountId: Long?): Collection<Subscription>

    suspend fun getIdSubscriptions(accountId: Long?): Collection<Subscription>

    suspend fun getSubscriptionsToId(accountId: Long?): Collection<Subscription>

    suspend fun getUpdateSubscriptions(timestamp: Int): Collection<Subscription>
}
