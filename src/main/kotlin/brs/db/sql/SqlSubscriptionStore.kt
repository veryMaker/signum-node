package brs.db.sql

import brs.db.BurstKey
import brs.db.MutableBatchEntityTable
import brs.db.SubscriptionStore
import brs.entity.DependencyProvider
import brs.entity.Subscription
import brs.schema.Tables.SUBSCRIPTION
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record

internal class SqlSubscriptionStore(private val dp: DependencyProvider) : SubscriptionStore {
    override val subscriptionDbKeyFactory = object : SqlDbKey.LongKeyFactory<Subscription>(SUBSCRIPTION.ID) {
        override fun newKey(entity: Subscription): BurstKey {
            return entity.dbKey
        }
    }

    override val subscriptionTable: MutableBatchEntityTable<Subscription>

    init {
        subscriptionTable = object : SqlMutableBatchEntityTable<Subscription>(SUBSCRIPTION, SUBSCRIPTION.HEIGHT, SUBSCRIPTION.LATEST, subscriptionDbKeyFactory, Subscription::class.java, dp) {
            override val defaultSort = listOf(
                SUBSCRIPTION.TIME_NEXT.asc(),
                SUBSCRIPTION.ID.asc()
            )

            override fun load(record: Record) = Subscription(
                record.get(SUBSCRIPTION.SENDER_ID),
                record.get(SUBSCRIPTION.RECIPIENT_ID),
                record.get(SUBSCRIPTION.ID),
                record.get(SUBSCRIPTION.AMOUNT),
                record.get(SUBSCRIPTION.FREQUENCY),
                record.get(SUBSCRIPTION.TIME_NEXT),
                subscriptionDbKeyFactory.newKey(record.get(SUBSCRIPTION.ID)))

            override fun saveBatch(ctx: DSLContext, entities: Collection<Subscription>) {
                val height = dp.blockchainService.height
                val query = ctx.insertInto(SUBSCRIPTION,
                    SUBSCRIPTION.ID,
                    SUBSCRIPTION.SENDER_ID,
                    SUBSCRIPTION.RECIPIENT_ID,
                    SUBSCRIPTION.AMOUNT,
                    SUBSCRIPTION.FREQUENCY,
                    SUBSCRIPTION.TIME_NEXT,
                    SUBSCRIPTION.HEIGHT,
                    SUBSCRIPTION.LATEST)
                entities.forEach { entity ->
                    query.values(
                        entity.id,
                        entity.senderId,
                        entity.recipientId,
                        entity.amountPlanck,
                        entity.frequency,
                        entity.timeNext,
                        height,
                        true
                    )
                }
                query.execute()
            }
        }
    }

    private fun getByParticipantClause(id: Long): Condition {
        return SUBSCRIPTION.SENDER_ID.eq(id).or(SUBSCRIPTION.RECIPIENT_ID.eq(id))
    }

    private fun getUpdateOnBlockClause(timestamp: Int): Condition {
        return SUBSCRIPTION.TIME_NEXT.le(timestamp)
    }

    override fun getSubscriptionsByParticipant(accountId: Long): Collection<Subscription> {
        return subscriptionTable.getManyBy(getByParticipantClause(accountId), 0, -1)
    }

    override fun getIdSubscriptions(accountId: Long): Collection<Subscription> {
        return subscriptionTable.getManyBy(SUBSCRIPTION.SENDER_ID.eq(accountId), 0, -1)
    }

    override fun getSubscriptionsToId(accountId: Long): Collection<Subscription> {
        return subscriptionTable.getManyBy(SUBSCRIPTION.RECIPIENT_ID.eq(accountId), 0, -1)
    }

    override fun getUpdateSubscriptions(timestamp: Int): Collection<Subscription> {
        return subscriptionTable.getManyBy(getUpdateOnBlockClause(timestamp), 0, -1)
    }
}
