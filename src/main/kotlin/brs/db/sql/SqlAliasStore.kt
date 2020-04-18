package brs.db.sql

import brs.db.AliasStore
import brs.db.BurstKey
import brs.db.MutableEntityTable
import brs.entity.Alias
import brs.entity.DependencyProvider
import brs.schema.Tables.ALIAS
import brs.schema.Tables.ALIAS_OFFER
import brs.util.convert.nullToZero
import org.jooq.DSLContext
import org.jooq.Record
import java.util.*

internal class SqlAliasStore(private val dp: DependencyProvider) : AliasStore {
    override val offerTable: MutableEntityTable<Alias.Offer>
    override val aliasTable: MutableEntityTable<Alias>
    override val aliasDbKeyFactory: SqlDbKey.LongKeyFactory<Alias> = AliasDbKeyFactory
    override val offerDbKeyFactory: SqlDbKey.LongKeyFactory<Alias.Offer> = OfferDbKeyFactory

    init {
        offerTable = object : SqlMutableBatchEntityTable<Alias.Offer>(ALIAS_OFFER, ALIAS_OFFER.HEIGHT, ALIAS_OFFER.LATEST, offerDbKeyFactory, Alias.Offer::class.java, dp) {
            override fun load(record: Record) = Alias.Offer(
                record.get(ALIAS_OFFER.ID),
                record.get(ALIAS_OFFER.PRICE),
                record.get(ALIAS_OFFER.BUYER_ID).nullToZero(),
                offerDbKeyFactory.newKey(record.get(ALIAS_OFFER.ID)))

            override fun saveBatch(ctx: DSLContext, entities: Collection<Alias.Offer>) {
                val height = dp.blockchainService.height
                val query = ctx.insertInto(ALIAS_OFFER, ALIAS_OFFER.ID, ALIAS_OFFER.PRICE, ALIAS_OFFER.BUYER_ID, ALIAS_OFFER.HEIGHT, ALIAS_OFFER.LATEST)
                entities.forEach { entity ->
                    query.values(
                        entity.id,
                        entity.pricePlanck,
                        if (entity.buyerId == 0L) null else entity.buyerId,
                        height,
                        true
                    )
                }
                query.execute()
            }
        }

        aliasTable = object : SqlMutableBatchEntityTable<Alias>(ALIAS, ALIAS.HEIGHT, ALIAS.LATEST, aliasDbKeyFactory, Alias::class.java, dp) {
                override val defaultSort = listOf(ALIAS.ALIAS_NAME_LOWER.asc())

                override fun load(record: Record) = Alias(
                    record.get(ALIAS.ID),
                    record.get(ALIAS.ACCOUNT_ID),
                    record.get(ALIAS.ALIAS_NAME),
                    record.get(ALIAS.ALIAS_URI),
                    record.get(ALIAS.TIMESTAMP),
                    aliasDbKeyFactory.newKey(record.get(ALIAS.ID)))

                override fun saveBatch(ctx: DSLContext, entities: Collection<Alias>) {
                    val height = dp.blockchainService.height
                    val query = ctx.insertInto(ALIAS, ALIAS.ID, ALIAS.ACCOUNT_ID, ALIAS.ALIAS_NAME, ALIAS.ALIAS_NAME_LOWER, ALIAS.ALIAS_URI, ALIAS.TIMESTAMP, ALIAS.HEIGHT)
                    entities.forEach { entity ->
                        query.values(entity.id, entity.accountId, entity.aliasName, entity.aliasName.toLowerCase(Locale.ENGLISH), entity.aliasURI, entity.timestamp, height)
                    }
                    query.execute()
                }
            }
    }

    override fun getAliasesByOwner(accountId: Long, from: Int, to: Int): Collection<Alias> {
        return aliasTable.getManyBy(ALIAS.ACCOUNT_ID.eq(accountId), from, to)
    }

    override fun getAlias(aliasName: String): Alias? {
        return aliasTable.getBy(ALIAS.ALIAS_NAME_LOWER.eq(aliasName.toLowerCase(Locale.ENGLISH)))
    }

    companion object {
        private object OfferDbKeyFactory : SqlDbKey.LongKeyFactory<Alias.Offer>(ALIAS_OFFER.ID) {
            override fun newKey(entity: Alias.Offer): BurstKey {
                return entity.dbKey
            }
        }

        private object AliasDbKeyFactory : SqlDbKey.LongKeyFactory<Alias>(ALIAS.ID) {
            override fun newKey(entity: Alias): BurstKey {
                return entity.dbKey
            }
        }
    }
}
