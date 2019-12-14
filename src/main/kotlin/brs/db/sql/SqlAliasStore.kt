package brs.db.sql

import brs.db.AliasStore
import brs.db.BurstKey
import brs.db.VersionedEntityTable
import brs.db.useDslContext
import brs.entity.Alias
import brs.entity.DependencyProvider
import brs.schema.Tables.ALIAS
import brs.schema.Tables.ALIAS_OFFER
import brs.schema.tables.records.AliasOfferRecord
import brs.schema.tables.records.AliasRecord
import brs.util.convert.nullToZero
import org.jooq.DSLContext
import org.jooq.Record
import java.util.*

internal class SqlAliasStore(private val dp: DependencyProvider) : AliasStore {
    override val offerTable: VersionedEntityTable<Alias.Offer>
    override val aliasTable: VersionedEntityTable<Alias>
    override val aliasDbKeyFactory = AliasDbKeyFactory
    override val offerDbKeyFactory = OfferDbKeyFactory

    init {
        offerTable = object : SqlVersionedEntityTable<Alias.Offer>(
            ALIAS_OFFER,
            ALIAS_OFFER.HEIGHT,
            ALIAS_OFFER.LATEST,
            offerDbKeyFactory,
            dp
        ) {
            override fun load(ctx: DSLContext, record: Record): Alias.Offer {
                return SqlOffer(record)
            }

            override fun save(ctx: DSLContext, offer: Alias.Offer) {
                saveOffer(offer)
            }
        }

        aliasTable =
            object : SqlVersionedEntityTable<Alias>(ALIAS, ALIAS.HEIGHT, ALIAS.LATEST, aliasDbKeyFactory, dp) {
                override val defaultSort = listOf(table.field("alias_name_lower", String::class.java).asc())

                override fun load(ctx: DSLContext, record: Record): Alias {
                    return SqlAlias(record)
                }

                override fun save(ctx: DSLContext, alias: Alias) {
                    saveAlias(ctx, alias)
                }
            }
    }

    private inner class SqlOffer internal constructor(record: Record) : Alias.Offer(
        record.get(ALIAS_OFFER.ID),
        record.get(ALIAS_OFFER.PRICE),
        record.get(ALIAS_OFFER.BUYER_ID).nullToZero(),
        offerDbKeyFactory.newKey(record.get(ALIAS_OFFER.ID))
    )

    private fun saveOffer(offer: Alias.Offer) {
        dp.db.useDslContext<Unit> { ctx ->
            ctx.insertInto<AliasOfferRecord, Long, Long, Long, Int>(
                ALIAS_OFFER,
                ALIAS_OFFER.ID,
                ALIAS_OFFER.PRICE,
                ALIAS_OFFER.BUYER_ID,
                ALIAS_OFFER.HEIGHT
            )
                .values(
                    offer.id,
                    offer.pricePlanck,
                    if (offer.buyerId == 0L) null else offer.buyerId,
                    dp.blockchainService.height
                )
                .execute()
        }
    }

    private inner class SqlAlias internal constructor(record: Record) : Alias(
        record.get(ALIAS.ID),
        record.get(ALIAS.ACCOUNT_ID),
        record.get(ALIAS.ALIAS_NAME),
        record.get(ALIAS.ALIAS_URI),
        record.get(ALIAS.TIMESTAMP),
        aliasDbKeyFactory.newKey(record.get(ALIAS.ID))
    )

    private fun saveAlias(ctx: DSLContext, alias: Alias) {
        ctx.insertInto<AliasRecord>(ALIAS).set(ALIAS.ID, alias.id).set(ALIAS.ACCOUNT_ID, alias.accountId)
            .set(ALIAS.ALIAS_NAME, alias.aliasName)
            .set(ALIAS.ALIAS_NAME_LOWER, alias.aliasName.toLowerCase(Locale.ENGLISH))
            .set(ALIAS.ALIAS_URI, alias.aliasURI).set(ALIAS.TIMESTAMP, alias.timestamp)
            .set(ALIAS.HEIGHT, dp.blockchainService.height).execute()
    }

    override fun getAliasesByOwner(accountId: Long, from: Int, to: Int): Collection<Alias> {
        return aliasTable.getManyBy(ALIAS.ACCOUNT_ID.eq(accountId), from, to)
    }

    override fun getAlias(aliasName: String): Alias? {
        return aliasTable.getBy(ALIAS.ALIAS_NAME_LOWER.eq(aliasName.toLowerCase(Locale.ENGLISH)))
    }

    companion object {
        internal object OfferDbKeyFactory : SqlDbKey.LongKeyFactory<Alias.Offer>(ALIAS_OFFER.ID) {
            override fun newKey(offer: Alias.Offer): BurstKey {
                return offer.dbKey
            }
        }

        internal object AliasDbKeyFactory : SqlDbKey.LongKeyFactory<Alias>(ALIAS.ID) {
            override fun newKey(alias: Alias): BurstKey {
                return alias.dbKey
            }
        }
    }
}
