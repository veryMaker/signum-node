package brs.db.sql

import brs.DependencyProvider
import brs.entity.Subscription
import brs.db.BurstKey
import brs.db.VersionedEntityTable
import brs.db.store.SubscriptionStore
import brs.schema.Tables.SUBSCRIPTION
import brs.schema.tables.records.SubscriptionRecord
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SortField

class SqlSubscriptionStore(private val dp: DependencyProvider) : SubscriptionStore {

    override val subscriptionDbKeyFactory: BurstKey.LongKeyFactory<Subscription> = object : DbKey.LongKeyFactory<Subscription>(SUBSCRIPTION.ID) {
        override fun newKey(subscription: Subscription): BurstKey {
            return subscription.dbKey
        }
    }

    override val subscriptionTable: VersionedEntityTable<Subscription>

    init {
        subscriptionTable = object : VersionedEntitySqlTable<Subscription>("subscription", SUBSCRIPTION, subscriptionDbKeyFactory, dp) {
            override fun load(ctx: DSLContext, rs: Record): Subscription {
                return SqlSubscription(rs)
            }

            override fun save(ctx: DSLContext, subscription: Subscription) {
                saveSubscription(ctx, subscription)
            }

            override fun defaultSort(): Collection<SortField<*>> {
                return listOf(tableClass.field("time_next", Int::class.java).asc(), tableClass.field("id", Long::class.java).asc())
            }
        }
    }

    private fun getByParticipantClause(id: Long): Condition {
        return SUBSCRIPTION.SENDER_ID.eq(id).or(SUBSCRIPTION.RECIPIENT_ID.eq(id))
    }

    private fun getUpdateOnBlockClause(timestamp: Int): Condition {
        return SUBSCRIPTION.TIME_NEXT.le(timestamp)
    }

    override fun getSubscriptionsByParticipant(accountId: Long?): Collection<Subscription> {
        return subscriptionTable.getManyBy(getByParticipantClause(accountId!!), 0, -1)
    }

    override fun getIdSubscriptions(accountId: Long?): Collection<Subscription> {
        return subscriptionTable.getManyBy(SUBSCRIPTION.SENDER_ID.eq(accountId), 0, -1)
    }

    override fun getSubscriptionsToId(accountId: Long?): Collection<Subscription> {
        return subscriptionTable.getManyBy(SUBSCRIPTION.RECIPIENT_ID.eq(accountId), 0, -1)
    }

    override fun getUpdateSubscriptions(timestamp: Int): Collection<Subscription> {
        return subscriptionTable.getManyBy(getUpdateOnBlockClause(timestamp), 0, -1)
    }

    private fun saveSubscription(ctx: DSLContext, subscription: Subscription) {
        ctx.mergeInto<SubscriptionRecord, Long, Long, Long, Long, Int, Int, Int, Boolean>(SUBSCRIPTION, SUBSCRIPTION.ID, SUBSCRIPTION.SENDER_ID, SUBSCRIPTION.RECIPIENT_ID, SUBSCRIPTION.AMOUNT, SUBSCRIPTION.FREQUENCY, SUBSCRIPTION.TIME_NEXT, SUBSCRIPTION.HEIGHT, SUBSCRIPTION.LATEST)
                .key(SUBSCRIPTION.ID, SUBSCRIPTION.SENDER_ID, SUBSCRIPTION.RECIPIENT_ID, SUBSCRIPTION.AMOUNT, SUBSCRIPTION.FREQUENCY, SUBSCRIPTION.TIME_NEXT, SUBSCRIPTION.HEIGHT, SUBSCRIPTION.LATEST)
                .values(subscription.id, subscription.senderId, subscription.recipientId, subscription.amountPlanck, subscription.frequency, subscription.timeNext, dp.blockchainService.height, true)
                .execute()
    }

    private inner class SqlSubscription internal constructor(record: Record) : Subscription(record.get(SUBSCRIPTION.SENDER_ID), record.get(SUBSCRIPTION.RECIPIENT_ID), record.get(SUBSCRIPTION.ID), record.get(SUBSCRIPTION.AMOUNT), record.get(SUBSCRIPTION.FREQUENCY), record.get(SUBSCRIPTION.TIME_NEXT), subscriptionDbKeyFactory.newKey(record.get(SUBSCRIPTION.ID)))
}
