package brs.db.sql

import brs.db.*
import brs.entity.DependencyProvider
import brs.entity.Order
import brs.schema.Tables.ASK_ORDER
import brs.schema.Tables.BID_ORDER
import brs.schema.tables.records.AskOrderRecord
import brs.schema.tables.records.BidOrderRecord
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
            override val defaultSort = listOf(table.field("creation_height", Int::class.java).desc())

            override fun load(ctx: DSLContext, record: Record): Order.Ask {
                return sqlToAsk(record)
            }

            override fun save(ctx: DSLContext, entity: Order.Ask) {
                saveAsk(ctx, entity)
            }
        }

        bidOrderTable = object : SqlVersionedEntityTable<Order.Bid>(
            BID_ORDER,
            BID_ORDER.HEIGHT,
            BID_ORDER.LATEST,
            bidOrderDbKeyFactory,
            dp
        ) {
            override val defaultSort = listOf(table.field("creation_height", Int::class.java).desc())

            override fun load(ctx: DSLContext, record: Record): Order.Bid {
                return sqlToBid(record)
            }

            override fun save(ctx: DSLContext, entity: Order.Bid) {
                saveBid(ctx, entity)
            }
        }
    }

    override fun getAskOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Order.Ask> {
        return askOrderTable.getManyBy(ASK_ORDER.ACCOUNT_ID.eq(accountId).and(ASK_ORDER.ASSET_ID.eq(assetId)), from, to)
    }

    private val askSort = listOf(
        ASK_ORDER.field("price", Long::class.java).asc(),
        ASK_ORDER.field("creation_height", Int::class.java).asc(),
        ASK_ORDER.field("id", Long::class.java).asc()
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

    private fun saveAsk(ctx: DSLContext, ask: Order.Ask) {
        val record = AskOrderRecord()
        record.id = ask.id
        record.accountId = ask.accountId
        record.assetId = ask.assetId
        record.price = ask.pricePlanck
        record.quantity = ask.quantity
        record.creationHeight = ask.height
        record.height = dp.blockchainService.height
        record.latest = true
        ctx.upsert(record, ASK_ORDER.ID, ASK_ORDER.HEIGHT).execute()
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
        BID_ORDER.field("price", Long::class.java).desc(),
        BID_ORDER.field("creation_height", Int::class.java).asc(),
        BID_ORDER.field("id", Long::class.java).asc()
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

    private fun saveBid(ctx: DSLContext, bid: Order.Bid) {
        val record = BidOrderRecord()
        record.id = bid.id
        record.accountId = bid.accountId
        record.assetId = bid.assetId
        record.price = bid.pricePlanck
        record.quantity = bid.quantity
        record.creationHeight = bid.height
        record.height = dp.blockchainService.height
        record.latest = true
        ctx.upsert(record, BID_ORDER.ID, BID_ORDER.HEIGHT).execute()
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
