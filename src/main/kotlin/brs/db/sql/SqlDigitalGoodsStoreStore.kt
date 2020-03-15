package brs.db.sql

import brs.db.*
import brs.entity.DependencyProvider
import brs.entity.Goods
import brs.entity.Purchase
import brs.schema.Tables.*
import burst.kit.entity.BurstEncryptedMessage
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record

internal class SqlDigitalGoodsStoreStore(private val dp: DependencyProvider) : DigitalGoodsStoreStore {
    override val feedbackDbKeyFactory = object : SqlDbKey.LongKeyFactory<Purchase>(PURCHASE_FEEDBACK.ID) {
        override fun newKey(entity: Purchase): BurstKey {
            return entity.dbKey
        }
    }

    override val purchaseDbKeyFactory = object : SqlDbKey.LongKeyFactory<Purchase>(PURCHASE.ID) {
        override fun newKey(entity: Purchase): BurstKey {
            return entity.dbKey
        }
    }

    override val purchaseTable: VersionedEntityTable<Purchase>

    override val feedbackTable: ValuesTable<Purchase, BurstEncryptedMessage>

    override val publicFeedbackDbKeyFactory = object : SqlDbKey.LongKeyFactory<Purchase>(PURCHASE_PUBLIC_FEEDBACK.ID) {
        override fun newKey(entity: Purchase): BurstKey {
            return entity.dbKey
        }
    }

    override val publicFeedbackTable: ValuesTable<Purchase, String>

    override val goodsDbKeyFactory = object : SqlDbKey.LongKeyFactory<Goods>(GOODS.ID) {
        override fun newKey(entity: Goods): BurstKey {
            return entity.dbKey
        }
    }

    override val goodsTable: VersionedEntityTable<Goods>

    init {
        purchaseTable = object : SqlVersionedEntityTable<Purchase>(
            PURCHASE,
            PURCHASE.HEIGHT,
            PURCHASE.LATEST,
            purchaseDbKeyFactory,
            dp
        ) {
            override val defaultSort = listOf(
                PURCHASE.TIMESTAMP.desc(),
                PURCHASE.ID.asc()
            )

            override fun load(record: Record): Purchase {
                return sqlToPurchase(record)
            }

            private val upsertColumns = listOf(PURCHASE.ID, PURCHASE.BUYER_ID, PURCHASE.GOODS_ID, PURCHASE.SELLER_ID, PURCHASE.QUANTITY, PURCHASE.PRICE, PURCHASE.DEADLINE, PURCHASE.NOTE, PURCHASE.NONCE, PURCHASE.PENDING, PURCHASE.TIMESTAMP, PURCHASE.GOODS, PURCHASE.GOODS_NONCE, PURCHASE.REFUND_NOTE, PURCHASE.REFUND_NONCE, PURCHASE.HAS_FEEDBACK_NOTES, PURCHASE.HAS_PUBLIC_FEEDBACKS, PURCHASE.DISCOUNT, PURCHASE.REFUND, PURCHASE.HEIGHT, PURCHASE.LATEST)
            private val upsertKeys = listOf(PURCHASE.ID, PURCHASE.HEIGHT)

            override fun save(ctx: DSLContext, entity: Purchase) {
                var note: ByteArray? = null
                var nonce: ByteArray? = null
                var goods: ByteArray? = null
                var goodsNonce: ByteArray? = null
                var refundNote: ByteArray? = null
                var refundNonce: ByteArray? = null
                if (entity.note != null) {
                    note = entity.note.data
                    nonce = entity.note.nonce
                }
                if (entity.encryptedGoods != null) {
                    goods = entity.encryptedGoods!!.data
                    goodsNonce = entity.encryptedGoods!!.nonce
                }
                if (entity.refundNote != null) {
                    refundNote = entity.refundNote!!.data
                    refundNonce = entity.refundNote!!.nonce
                }
                ctx.upsert(PURCHASE, upsertKeys, mapOf(
                    PURCHASE.ID to entity.id,
                    PURCHASE.BUYER_ID to entity.buyerId,
                    PURCHASE.GOODS_ID to entity.goodsId,
                    PURCHASE.SELLER_ID to entity.sellerId,
                    PURCHASE.QUANTITY to entity.quantity,
                    PURCHASE.PRICE to entity.pricePlanck,
                    PURCHASE.DEADLINE to entity.deliveryDeadlineTimestamp,
                    PURCHASE.NOTE to note,
                    PURCHASE.NONCE to nonce,
                    PURCHASE.PENDING to entity.isPending,
                    PURCHASE.TIMESTAMP to entity.timestamp,
                    PURCHASE.GOODS to goods,
                    PURCHASE.GOODS_NONCE to goodsNonce,
                    PURCHASE.REFUND_NOTE to refundNote,
                    PURCHASE.REFUND_NONCE to refundNonce,
                    PURCHASE.HAS_FEEDBACK_NOTES to entity.feedbackNotes.isNotEmpty(),
                    PURCHASE.HAS_PUBLIC_FEEDBACKS to entity.publicFeedback.isNotEmpty(),
                    PURCHASE.DISCOUNT to entity.discountPlanck,
                    PURCHASE.REFUND to entity.refundPlanck,
                    PURCHASE.HEIGHT to dp.blockchainService.height,
                    PURCHASE.LATEST to true
                )).execute()
            }

            override fun save(ctx: DSLContext, entities: Collection<Purchase>) {
                val height = dp.blockchainService.height
                ctx.upsert(PURCHASE, upsertColumns, upsertKeys, entities.map { entity ->
                    var note: ByteArray? = null
                    var nonce: ByteArray? = null
                    var goods: ByteArray? = null
                    var goodsNonce: ByteArray? = null
                    var refundNote: ByteArray? = null
                    var refundNonce: ByteArray? = null
                    if (entity.note != null) {
                        note = entity.note.data
                        nonce = entity.note.nonce
                    }
                    if (entity.encryptedGoods != null) {
                        goods = entity.encryptedGoods!!.data
                        goodsNonce = entity.encryptedGoods!!.nonce
                    }
                    if (entity.refundNote != null) {
                        refundNote = entity.refundNote!!.data
                        refundNonce = entity.refundNote!!.nonce
                    }

                    listOf(entity.id,
                        entity.buyerId,
                        entity.goodsId,
                        entity.sellerId,
                        entity.quantity,
                        entity.pricePlanck,
                        entity.deliveryDeadlineTimestamp,
                        note,
                        nonce,
                        entity.isPending,
                        entity.timestamp,
                        goods,
                        goodsNonce,
                        refundNote,
                        refundNonce,
                        entity.feedbackNotes.isNotEmpty(),
                        entity.publicFeedback.isNotEmpty(),
                        entity.discountPlanck,
                        entity.refundPlanck,
                        height,
                        true)
                }).execute()
            }
        }

        feedbackTable = object : SqlVersionedValuesTable<Purchase, BurstEncryptedMessage>(
            PURCHASE_FEEDBACK,
            PURCHASE_FEEDBACK.HEIGHT,
            PURCHASE_FEEDBACK.LATEST,
            feedbackDbKeyFactory,
            dp
        ) {
            override fun load(ctx: DSLContext, record: Record): BurstEncryptedMessage {
                val data = record.get(PURCHASE_FEEDBACK.FEEDBACK_DATA)
                val nonce = record.get(PURCHASE_FEEDBACK.FEEDBACK_NONCE)
                return BurstEncryptedMessage(data, nonce, false)
            }

            override fun save(ctx: DSLContext, key: Purchase, values: List<BurstEncryptedMessage>) {
                val height = dp.blockchainService.height
                val query = ctx.insertInto(PURCHASE_FEEDBACK, PURCHASE_FEEDBACK.ID, PURCHASE_FEEDBACK.FEEDBACK_DATA, PURCHASE_FEEDBACK.FEEDBACK_NONCE, PURCHASE_FEEDBACK.HEIGHT, PURCHASE_FEEDBACK.LATEST)
                values.forEach { value ->
                    query.values(key.id, value.data, value.nonce, height, true)
                }
                query.execute()
            }
        }

        publicFeedbackTable = object : SqlVersionedValuesTable<Purchase, String>(
            PURCHASE_PUBLIC_FEEDBACK,
            PURCHASE_PUBLIC_FEEDBACK.HEIGHT,
            PURCHASE_PUBLIC_FEEDBACK.LATEST,
            publicFeedbackDbKeyFactory,
            dp
        ) {
            override fun load(ctx: DSLContext, record: Record): String {
                return record.get(PURCHASE_PUBLIC_FEEDBACK.PUBLIC_FEEDBACK)
            }

            private val upsertColumns = listOf(PURCHASE_PUBLIC_FEEDBACK.ID, PURCHASE_PUBLIC_FEEDBACK.PUBLIC_FEEDBACK, PURCHASE_PUBLIC_FEEDBACK.HEIGHT, PURCHASE_PUBLIC_FEEDBACK.LATEST)
            private val upsertKeys = listOf(PURCHASE_PUBLIC_FEEDBACK.ID, PURCHASE_PUBLIC_FEEDBACK.HEIGHT)

            override fun save(ctx: DSLContext, key: Purchase, values: List<String>) {
                val height = dp.blockchainService.height
                ctx.upsert(PURCHASE_PUBLIC_FEEDBACK,
                    upsertColumns,
                    upsertKeys,
                    values.map { publicFeedback -> listOf(key.id, publicFeedback, height, true) }).execute()
            }
        }

        goodsTable =
            object : SqlVersionedEntityTable<Goods>(GOODS, GOODS.HEIGHT, GOODS.LATEST, goodsDbKeyFactory, dp) {
                override val defaultSort = listOf(
                    GOODS.TIMESTAMP.desc(),
                    GOODS.ID.asc()
                )

                override fun load(record: Record): Goods {
                    return sqlToGoods(record)
                }

                private val upsertColumns = listOf(GOODS.ID, GOODS.SELLER_ID, GOODS.NAME, GOODS.DESCRIPTION, GOODS.TAGS, GOODS.TIMESTAMP, GOODS.QUANTITY, GOODS.PRICE, GOODS.DELISTED, GOODS.HEIGHT, GOODS.LATEST)
                private val upsertKeys = listOf(GOODS.ID, GOODS.HEIGHT)

                override fun save(ctx: DSLContext, entity: Goods) {
                    ctx.upsert(GOODS, upsertKeys, mapOf(
                        GOODS.ID to entity.id,
                        GOODS.SELLER_ID to entity.sellerId,
                        GOODS.NAME to entity.name,
                        GOODS.DESCRIPTION to entity.description,
                        GOODS.TAGS to entity.tags,
                        GOODS.TIMESTAMP to entity.timestamp,
                        GOODS.QUANTITY to entity.quantity,
                        GOODS.PRICE to entity.pricePlanck,
                        GOODS.DELISTED to entity.isDelisted,
                        GOODS.HEIGHT to dp.blockchainService.height,
                        GOODS.LATEST to true
                    )).execute()
                }

                override fun save(ctx: DSLContext, entities: Collection<Goods>) {
                    val height = dp.blockchainService.height
                    ctx.upsert(GOODS, upsertColumns, upsertKeys, entities.map { entity -> listOf(
                        entity.id,
                        entity.sellerId,
                        entity.name,
                        entity.description,
                        entity.tags,
                        entity.timestamp,
                        entity.quantity,
                        entity.pricePlanck,
                        entity.isDelisted,
                        height,
                        true
                    ) }).execute()
                }
            }
    }

    override fun getExpiredPendingPurchases(timestamp: Int): Collection<Purchase> {
        return purchaseTable.getManyBy(PURCHASE.DEADLINE.lt(timestamp).and(PURCHASE.PENDING.isTrue), 0, -1)
    }

    private fun loadEncryptedData(
        record: Record,
        dataField: Field<ByteArray>,
        nonceField: Field<ByteArray>
    ): BurstEncryptedMessage? {
        return BurstEncryptedMessage(record.get(dataField) ?: return null, record.get(nonceField) ?: return null, false)
    }

    override fun getGoodsInStock(from: Int, to: Int): Collection<Goods> {
        return goodsTable.getManyBy(GOODS.DELISTED.isFalse.and(GOODS.QUANTITY.gt(0)), from, to)
    }

    private val sellerGoodsSort = listOf(
        GOODS.NAME.asc(),
        GOODS.TIMESTAMP.desc(),
        GOODS.ID.asc()
    )

    override fun getSellerGoods(sellerId: Long, inStockOnly: Boolean, from: Int, to: Int): Collection<Goods> {
        val condition = if (inStockOnly) GOODS.SELLER_ID.eq(sellerId).and(GOODS.DELISTED.isFalse).and(GOODS.QUANTITY.gt(0)) else GOODS.SELLER_ID.eq(sellerId)
        return goodsTable.getManyBy(condition, from, to, sellerGoodsSort)
    }

    override fun getAllPurchases(from: Int, to: Int): Collection<Purchase> {
        return purchaseTable.getAll(from, to)
    }

    override fun getSellerPurchases(sellerId: Long, from: Int, to: Int): Collection<Purchase> {
        return purchaseTable.getManyBy(PURCHASE.SELLER_ID.eq(sellerId), from, to)
    }

    override fun getBuyerPurchases(buyerId: Long, from: Int, to: Int): Collection<Purchase> {
        return purchaseTable.getManyBy(PURCHASE.BUYER_ID.eq(buyerId), from, to)
    }

    override fun getSellerBuyerPurchases(sellerId: Long, buyerId: Long, from: Int, to: Int): Collection<Purchase> {
        return purchaseTable.getManyBy(PURCHASE.SELLER_ID.eq(sellerId).and(PURCHASE.BUYER_ID.eq(buyerId)), from, to)
    }

    override fun getPendingSellerPurchases(sellerId: Long, from: Int, to: Int): Collection<Purchase> {
        return purchaseTable.getManyBy(PURCHASE.SELLER_ID.eq(sellerId).and(PURCHASE.PENDING.isTrue), from, to)
    }

    private fun sqlToGoods(record: Record) = Goods(
        record.get(GOODS.ID),
        goodsDbKeyFactory.newKey(record.get(GOODS.ID)),
        record.get(GOODS.SELLER_ID),
        record.get(GOODS.NAME),
        record.get(GOODS.DESCRIPTION),
        record.get(GOODS.TAGS),
        record.get(GOODS.TIMESTAMP),
        record.get(GOODS.QUANTITY),
        record.get(GOODS.PRICE),
        record.get(GOODS.DELISTED))

    private fun sqlToPurchase(record: Record) = Purchase(
        dp,
        record.get(PURCHASE.ID),
        purchaseDbKeyFactory.newKey(record.get(PURCHASE.ID)),
        record.get(PURCHASE.BUYER_ID),
        record.get(PURCHASE.GOODS_ID),
        record.get(PURCHASE.SELLER_ID),
        record.get(PURCHASE.QUANTITY),
        record.get(PURCHASE.PRICE),
        record.get(PURCHASE.DEADLINE),
        loadEncryptedData(record, PURCHASE.NOTE, PURCHASE.NONCE),
        record.get(PURCHASE.TIMESTAMP),
        record.get(PURCHASE.PENDING),
        loadEncryptedData(record, PURCHASE.GOODS, PURCHASE.GOODS_NONCE),
        loadEncryptedData(record, PURCHASE.REFUND_NOTE, PURCHASE.REFUND_NONCE),
        record.get(PURCHASE.HAS_FEEDBACK_NOTES),
        record.get(PURCHASE.HAS_PUBLIC_FEEDBACKS),
        record.get(PURCHASE.DISCOUNT),
        record.get(PURCHASE.REFUND))
}
