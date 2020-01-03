package brs.db.sql

import brs.db.*
import brs.entity.DependencyProvider
import brs.entity.Goods
import brs.entity.Purchase
import brs.schema.Tables.*
import brs.schema.tables.records.GoodsRecord
import brs.schema.tables.records.PurchaseFeedbackRecord
import brs.schema.tables.records.PurchasePublicFeedbackRecord
import brs.schema.tables.records.PurchaseRecord
import burst.kit.entity.BurstEncryptedMessage
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record

internal class SqlDigitalGoodsStoreStore(private val dp: DependencyProvider) : DigitalGoodsStoreStore {
    override val feedbackDbKeyFactory = object : SqlDbKey.LongKeyFactory<Purchase>(PURCHASE.ID) {
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

    override val publicFeedbackDbKeyFactory = object : SqlDbKey.LongKeyFactory<Purchase>(PURCHASE.ID) {
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
                table.field("timestamp", Int::class.java).desc(),
                table.field("id", Long::class.java).asc()
            )

            override fun load(ctx: DSLContext, record: Record): Purchase {
                return sqlToPurchase(record)
            }

            override fun save(ctx: DSLContext, entity: Purchase) {
                savePurchase(ctx, entity)
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

            override fun save(ctx: DSLContext, key: Purchase, value: BurstEncryptedMessage) {
                ctx.insertInto<PurchaseFeedbackRecord, Long, ByteArray, ByteArray, Int, Boolean>(
                    PURCHASE_FEEDBACK,
                    PURCHASE_FEEDBACK.ID,
                    PURCHASE_FEEDBACK.FEEDBACK_DATA, PURCHASE_FEEDBACK.FEEDBACK_NONCE,
                    PURCHASE_FEEDBACK.HEIGHT, PURCHASE_FEEDBACK.LATEST
                ).values(
                    key.id,
                    value.data, value.nonce,
                    dp.blockchainService.height, true
                ).execute()
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

            override fun save(ctx: DSLContext, key: Purchase, value: String) {
                val record = PurchasePublicFeedbackRecord()
                record.id = key.id
                record.publicFeedback = value
                record.height = dp.blockchainService.height
                record.latest = true
                ctx.upsert(record, PURCHASE_PUBLIC_FEEDBACK.ID, PURCHASE_PUBLIC_FEEDBACK.HEIGHT).execute()
            }
        }

        goodsTable =
            object : SqlVersionedEntityTable<Goods>(GOODS, GOODS.HEIGHT, GOODS.LATEST, goodsDbKeyFactory, dp) {
                override val defaultSort = listOf(
                    table.field("timestamp", Int::class.java).desc(),
                    table.field("id", Long::class.java).asc()
                )

                override fun load(ctx: DSLContext, record: Record): Goods {
                    return sqlToGoods(record)
                }

                override fun save(ctx: DSLContext, entity: Goods) {
                    val record = GoodsRecord()
                    record.id = entity.id
                    record.sellerId = entity.sellerId
                    record.name = entity.name
                    record.description = entity.description
                    record.tags = entity.tags
                    record.timestamp = entity.timestamp
                    record.quantity = entity.quantity
                    record.price = entity.pricePlanck
                    record.delisted = entity.isDelisted
                    record.height = dp.blockchainService.height
                    record.latest = true
                    ctx.upsert(record, GOODS.ID, GOODS.HEIGHT).execute()
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

    private fun savePurchase(ctx: DSLContext, purchase: Purchase) {
        var note: ByteArray? = null
        var nonce: ByteArray? = null
        var goods: ByteArray? = null
        var goodsNonce: ByteArray? = null
        var refundNote: ByteArray? = null
        var refundNonce: ByteArray? = null
        if (purchase.note != null) {
            note = purchase.note.data
            nonce = purchase.note.nonce
        }
        if (purchase.encryptedGoods != null) {
            goods = purchase.encryptedGoods!!.data
            goodsNonce = purchase.encryptedGoods!!.nonce
        }
        if (purchase.refundNote != null) {
            refundNote = purchase.refundNote!!.data
            refundNonce = purchase.refundNote!!.nonce
        }
        val record = PurchaseRecord()
        record.id = purchase.id
        record.buyerId = purchase.buyerId
        record.goodsId = purchase.goodsId
        record.sellerId = purchase.sellerId
        record.quantity = purchase.quantity
        record.price = purchase.pricePlanck
        record.deadline = purchase.deliveryDeadlineTimestamp
        record.note = note
        record.nonce = nonce
        record.pending = purchase.isPending
        record.timestamp = purchase.timestamp
        record.goods = goods
        record.goodsNonce = goodsNonce
        record.refundNote = refundNote
        record.refundNonce = refundNonce
        record.hasFeedbackNotes = purchase.feedbackNotes.isNotEmpty()
        record.hasPublicFeedbacks = purchase.publicFeedback.isNotEmpty()
        record.discount = purchase.discountPlanck
        record.refund = purchase.refundPlanck
        record.height = dp.blockchainService.height
        record.latest = true
        ctx.upsert(record, PURCHASE.ID, PURCHASE.HEIGHT).execute()
    }

    override fun getGoodsInStock(from: Int, to: Int): Collection<Goods> {
        return goodsTable.getManyBy(GOODS.DELISTED.isFalse.and(GOODS.QUANTITY.gt(0)), from, to)
    }

    override fun getSellerGoods(sellerId: Long, inStockOnly: Boolean, from: Int, to: Int): Collection<Goods> {
        return goodsTable.getManyBy(
            if (inStockOnly) GOODS.SELLER_ID.eq(sellerId).and(GOODS.DELISTED.isFalse).and(
                GOODS.QUANTITY.gt(
                    0
                )
            ) else GOODS.SELLER_ID.eq(sellerId),
            from,
            to,
            listOf(
                GOODS.field("name", String::class.java).asc(),
                GOODS.field("timestamp", Int::class.java).desc(),
                GOODS.field("id", Long::class.java).asc()
            )
        )
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
