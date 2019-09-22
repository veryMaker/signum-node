package brs.db.sql

import brs.Alias
import brs.DependencyProvider
import brs.db.BurstKey
import brs.db.VersionedEntityTable
import brs.db.store.AliasStore
import brs.schema.Tables.ALIAS
import brs.schema.Tables.ALIAS_OFFER
import brs.schema.tables.records.AliasOfferRecord
import brs.schema.tables.records.AliasRecord
import brs.util.Convert
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SortField
import java.util.*

class SqlAliasStore(private val dp: DependencyProvider) : AliasStore {
    override val offerTable: VersionedEntityTable<Alias.Offer>
    override val aliasTable: VersionedEntityTable<Alias>
    override val aliasDbKeyFactory = AliasDbKeyFactory
    override val offerDbKeyFactory = OfferDbKeyFactory

    init {
        offerTable = object : VersionedEntitySqlTable<Alias.Offer>("alias_offer", ALIAS_OFFER, offerDbKeyFactory, dp) {
            override fun load(ctx: DSLContext, record: Record): Alias.Offer {
                return SqlOffer(record)
            }

            override fun save(ctx: DSLContext, offer: Alias.Offer) {
                saveOffer(offer)
            }
        }

        aliasTable = object : VersionedEntitySqlTable<Alias>("alias", brs.schema.Tables.ALIAS, aliasDbKeyFactory, dp) {
            override fun load(ctx: DSLContext, record: Record): Alias {
                return SqlAlias(record)
            }

            override fun save(ctx: DSLContext, alias: Alias) {
                saveAlias(ctx, alias)
            }

            override fun defaultSort(): List<SortField<*>> {
                val sort = mutableListOf<SortField<*>>()
                sort.add(tableClass.field("alias_name_lower", String::class.java).asc())
                return sort
            }
        }
    }

    private inner class SqlOffer internal constructor(record: Record) : Alias.Offer(record.get(ALIAS_OFFER.ID), record.get(ALIAS_OFFER.PRICE), Convert.nullToZero(record.get(ALIAS_OFFER.BUYER_ID)), offerDbKeyFactory.newKey(record.get(ALIAS_OFFER.ID)))

    private fun saveOffer(offer: Alias.Offer) {
        Db.useDSLContext { ctx ->
            ctx.insertInto<AliasOfferRecord, Long, Long, Long, Int>(ALIAS_OFFER, ALIAS_OFFER.ID, ALIAS_OFFER.PRICE, ALIAS_OFFER.BUYER_ID, ALIAS_OFFER.HEIGHT)
                    .values(offer.id, offer.priceNQT, if (offer.buyerId == 0L) null else offer.buyerId, dp.blockchain.height)
                    .execute()
        }
    }

    private inner class SqlAlias internal constructor(record: Record) : Alias(record.get(ALIAS.ID), record.get(ALIAS.ACCOUNT_ID), record.get(ALIAS.ALIAS_NAME), record.get(ALIAS.ALIAS_URI), record.get(ALIAS.TIMESTAMP), aliasDbKeyFactory.newKey(record.get(ALIAS.ID)))

    private fun saveAlias(ctx: DSLContext, alias: Alias) {
        ctx.insertInto<AliasRecord>(ALIAS).set(ALIAS.ID, alias.id).set(ALIAS.ACCOUNT_ID, alias.accountId).set(ALIAS.ALIAS_NAME, alias.aliasName).set(ALIAS.ALIAS_NAME_LOWER, alias.aliasName.toLowerCase(Locale.ENGLISH)).set(ALIAS.ALIAS_URI, alias.aliasURI).set(ALIAS.TIMESTAMP, alias.timestamp).set(ALIAS.HEIGHT, dp.blockchain.height).execute()
    }

    override fun getAliasesByOwner(accountId: Long, from: Int, to: Int): Collection<Alias> {
        return aliasTable.getManyBy(brs.schema.Tables.ALIAS.ACCOUNT_ID.eq(accountId), from, to)
    }

    override fun getAlias(aliasName: String): Alias? {
        return aliasTable.getBy(brs.schema.Tables.ALIAS.ALIAS_NAME_LOWER.eq(aliasName.toLowerCase(Locale.ENGLISH)))
    }

    companion object {
        object OfferDbKeyFactory : DbKey.LongKeyFactory<Alias.Offer>(ALIAS_OFFER.ID) {
            override fun newKey(offer: Alias.Offer): BurstKey {
                return offer.dbKey
            }
        }

        object AliasDbKeyFactory : DbKey.LongKeyFactory<Alias>(ALIAS.ID) {
            override fun newKey(alias: Alias): BurstKey {
                return alias.dbKey
            }
        }
    }
}
