package brs.db.sql

import brs.db.*
import brs.entity.DependencyProvider
import brs.entity.Order
import brs.schema.Tables.ASK_ORDER
import brs.schema.Tables.BID_ORDER
import org.jooq.DSLContext
import org.jooq.Record

internal class SqlOrderStore(private val dp: DependencyProvider) : OrderStore {
    override val askOrderDbKeyFactory = object : SqlDbKey.LongKeyFactory<Order.Ask>(ASK_ORDER.ID) {
        override fun newKey(entity: Order.Ask): BurstKey {
            return entity.dbKey
        }
    }
    override val askOrderTable: VersionedEntityTable<Order.Ask>
    override val bidOrderDbKeyFactory = object : SqlDbKey.LongKeyFactory<Order.Bid>(BID_ORDER.ID) {
        override fun newKey(entity: Order.Bid): BurstKey {
            return entity.dbKey
        }
    }
    override val bidOrderTable: VersionedEntityTable<Order.Bid>

    init {
        askOrderTable = object : SqlVersionedEntityTable<Order.Ask>(
            ASK_ORDER,
            ASK_ORDER.HEIGHT,
            ASK_ORDER.LATEST,
            askOrderDbKeyFactory,
            dp
        ) {
            override val defaultSort = listOf(ASK_ORDER.CREATION_HEIGHT.desc())

            override fun load(record: Record): Order.Ask {
                return sqlToAsk(record)
            }

            private val upsertColumns = listOf(ASK_ORDER.ID, ASK_ORDER.ACCOUNT_ID, ASK_ORDER.ASSET_ID, ASK_ORDER.PRICE, ASK_ORDER.QUANTITY, ASK_ORDER.CREATION_HEIGHT, ASK_ORDER.HEIGHT, ASK_ORDER.LATEST)
            private val upsertKeys = listOf(ASK_ORDER.ID, ASK_ORDER.HEIGHT)

            override fun save(ctx: DSLContext, entity: Order.Ask) {
                ctx.upsert(ASK_ORDER, upsertKeys, mapOf(
                    ASK_ORDER.ID to entity.id,
                    ASK_ORDER.ACCOUNT_ID to entity.accountId,
                    ASK_ORDER.ASSET_ID to entity.assetId,
                    ASK_ORDER.PRICE to entity.pricePlanck,
                    ASK_ORDER.QUANTITY to entity.quantity,
                    ASK_ORDER.CREATION_HEIGHT to entity.height,
                    ASK_ORDER.HEIGHT to dp.blockchainService.height,
                    ASK_ORDER.LATEST to true
                )).execute()
            }

            override fun save(ctx: DSLContext, entities: Collection<Order.Ask>) {
                val height = dp.blockchainService.height
                ctx.upsert(ASK_ORDER, upsertColumns, upsertKeys, entities.map { entity -> listOf(
                    entity.id,
                    entity.accountId,
                    entity.assetId,
                    entity.pricePlanck,
                    entity.quantity,
                    entity.height,
                    height,
                    true
                ) }).execute()
            }
        }

        bidOrderTable = object : SqlVersionedEntityTable<Order.Bid>(
            BID_ORDER,
            BID_ORDER.HEIGHT,
            BID_ORDER.LATEST,
            bidOrderDbKeyFactory,
            dp
        ) {
            override val defaultSort = listOf(BID_ORDER.CREATION_HEIGHT.desc())

            override fun load(record: Record): Order.Bid {
                return sqlToBid(record)
            }

            private val upsertColumns = listOf(BID_ORDER.ID, BID_ORDER.ACCOUNT_ID, BID_ORDER.ASSET_ID, BID_ORDER.PRICE, BID_ORDER.QUANTITY, BID_ORDER.CREATION_HEIGHT, BID_ORDER.HEIGHT, BID_ORDER.LATEST)
            private val upsertKeys = listOf(BID_ORDER.ID, BID_ORDER.HEIGHT)

            override fun save(ctx: DSLContext, entity: Order.Bid) {
                ctx.upsert(BID_ORDER, upsertKeys, mapOf(
                    BID_ORDER.ID to entity.id,
                    BID_ORDER.ACCOUNT_ID to entity.accountId,
                    BID_ORDER.ASSET_ID to entity.assetId,
                    BID_ORDER.PRICE to entity.pricePlanck,
                    BID_ORDER.QUANTITY to entity.quantity,
                    BID_ORDER.CREATION_HEIGHT to entity.height,
                    BID_ORDER.HEIGHT to dp.blockchainService.height,
                    BID_ORDER.LATEST to true
                )).execute()
            }

            override fun save(ctx: DSLContext, entities: Collection<Order.Bid>) {
                val height = dp.blockchainService.height
                ctx.upsert(BID_ORDER, upsertColumns, upsertKeys, entities.map { entity -> listOf(
                    entity.id,
                    entity.accountId,
                    entity.assetId,
                    entity.pricePlanck,
                    entity.quantity,
                    entity.height,
                    height,
                    true
                ) }).execute()
            }
        }
    }

    override fun getAskOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Order.Ask> {
        return askOrderTable.getManyBy(ASK_ORDER.ACCOUNT_ID.eq(accountId).and(ASK_ORDER.ASSET_ID.eq(assetId)), from, to)
    }

    private val askSort = listOf(
        ASK_ORDER.PRICE.asc(),
        ASK_ORDER.CREATION_HEIGHT.asc(),
        ASK_ORDER.ID.asc()
    )

    override fun getSortedAsks(assetId: Long, from: Int, to: Int): Collection<Order.Ask> {
        return askOrderTable.getManyBy(ASK_ORDER.ASSET_ID.eq(assetId), from, to, askSort)
    }

    override fun getNextOrder(assetId: Long): Order.Ask? {
        return dp.db.useDslContext { ctx ->
            val query = ctx.selectFrom(ASK_ORDER)
                .where(ASK_ORDER.ASSET_ID.eq(assetId).and(ASK_ORDER.LATEST.isTrue))
                .orderBy(
                    ASK_ORDER.PRICE.asc(),
                    ASK_ORDER.CREATION_HEIGHT.asc(),
                    ASK_ORDER.ID.asc()
                )
                .limit(1)
                .query
            val result = askOrderTable.getManyBy(ctx, query, true).iterator()
            if (result.hasNext()) result.next() else null
        }
    }

    override fun getAll(from: Int, to: Int): Collection<Order.Ask> {
        return askOrderTable.getAll(from, to)
    }

    override fun getAskOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Order.Ask> {
        return askOrderTable.getManyBy(ASK_ORDER.ACCOUNT_ID.eq(accountId), from, to)
    }

    override fun getAskOrdersByAsset(assetId: Long, from: Int, to: Int): Collection<Order.Ask> {
        return askOrderTable.getManyBy(ASK_ORDER.ASSET_ID.eq(assetId), from, to)
    }

    override fun getBidOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Order.Bid> {
        return bidOrderTable.getManyBy(BID_ORDER.ACCOUNT_ID.eq(accountId), from, to)
    }

    override fun getBidOrdersByAsset(assetId: Long, from: Int, to: Int): Collection<Order.Bid> {
        return bidOrderTable.getManyBy(BID_ORDER.ASSET_ID.eq(assetId), from, to)
    }

    override fun getBidOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Order.Bid> {
        return bidOrderTable.getManyBy(
            BID_ORDER.ACCOUNT_ID.eq(accountId).and(
                BID_ORDER.ASSET_ID.eq(assetId)
            ),
            from,
            to
        )
    }

    private val bidSort = listOf(
        BID_ORDER.PRICE.desc(),
        BID_ORDER.CREATION_HEIGHT.asc(),
        BID_ORDER.ID.asc()
    )

    override fun getSortedBids(assetId: Long, from: Int, to: Int): Collection<Order.Bid> {
        return bidOrderTable.getManyBy(BID_ORDER.ASSET_ID.eq(assetId), from, to, bidSort)
    }

    override fun getNextBid(assetId: Long): Order.Bid? {
        return dp.db.useDslContext { ctx ->
            val query = ctx.selectFrom(BID_ORDER)
                .where(
                    BID_ORDER.ASSET_ID.eq(assetId)
                        .and(BID_ORDER.LATEST.isTrue)
                )
                .orderBy(
                    BID_ORDER.PRICE.desc(),
                    BID_ORDER.CREATION_HEIGHT.asc(),
                    BID_ORDER.ID.asc()
                )
                .limit(1)
                .query
            val result = bidOrderTable.getManyBy(ctx, query, true).iterator()
            if (result.hasNext()) result.next() else null
        }
    }

    private fun sqlToAsk(record: Record) = Order.Ask(
        record.get(ASK_ORDER.ID),
        record.get(ASK_ORDER.ACCOUNT_ID),
        record.get(ASK_ORDER.ASSET_ID),
        record.get(ASK_ORDER.PRICE),
        record.get(ASK_ORDER.CREATION_HEIGHT),
        record.get(ASK_ORDER.QUANTITY),
        askOrderDbKeyFactory.newKey(record.get(ASK_ORDER.ID)))

    private fun sqlToBid(record: Record) = Order.Bid(
        record.get(BID_ORDER.ID),
        record.get(BID_ORDER.ACCOUNT_ID),
        record.get(BID_ORDER.ASSET_ID),
        record.get(BID_ORDER.PRICE),
        record.get(BID_ORDER.CREATION_HEIGHT),
        record.get(BID_ORDER.QUANTITY),
        bidOrderDbKeyFactory.newKey(record.get(BID_ORDER.ID)))
}
