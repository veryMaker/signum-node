package brs.db.sql

import brs.DependencyProvider
import brs.entity.DigitalGoodsStore
import brs.entity.EncryptedData
import brs.db.BurstKey
import brs.db.VersionedEntityTable
import brs.db.VersionedValuesTable
import brs.db.store.DigitalGoodsStoreStore
import brs.schema.Tables.*
import brs.schema.tables.records.GoodsRecord
import brs.schema.tables.records.PurchaseFeedbackRecord
import brs.schema.tables.records.PurchasePublicFeedbackRecord
import brs.schema.tables.records.PurchaseRecord
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.SortField

class SqlDigitalGoodsStoreStore(private val dp: DependencyProvider) : DigitalGoodsStoreStore {
    override val feedbackDbKeyFactory = object : DbKey.LongKeyFactory<DigitalGoodsStore.Purchase>(PURCHASE.ID) {
        override fun newKey(purchase: DigitalGoodsStore.Purchase): BurstKey {
            return purchase.dbKey
        }
    }

    override val purchaseDbKeyFactory: BurstKey.LongKeyFactory<DigitalGoodsStore.Purchase> = object : DbKey.LongKeyFactory<DigitalGoodsStore.Purchase>(PURCHASE.ID) {
        override fun newKey(purchase: DigitalGoodsStore.Purchase): BurstKey {
            return purchase.dbKey
        }
    }

    override val purchaseTable: VersionedEntityTable<DigitalGoodsStore.Purchase>

    @Deprecated("")
    override val feedbackTable: VersionedValuesTable<DigitalGoodsStore.Purchase, EncryptedData>

    override val publicFeedbackDbKeyFactory: DbKey.LongKeyFactory<DigitalGoodsStore.Purchase> = object : DbKey.LongKeyFactory<DigitalGoodsStore.Purchase>(PURCHASE.ID) {
        override fun newKey(purchase: DigitalGoodsStore.Purchase): BurstKey {
            return purchase.dbKey
        }
    }

    override val publicFeedbackTable: VersionedValuesTable<DigitalGoodsStore.Purchase, String>

    override val goodsDbKeyFactory: BurstKey.LongKeyFactory<DigitalGoodsStore.Goods> = object : DbKey.LongKeyFactory<DigitalGoodsStore.Goods>(GOODS.ID) {
        override fun newKey(goods: DigitalGoodsStore.Goods): BurstKey {
            return goods.dbKey
        }
    }

    override val goodsTable: VersionedEntityTable<DigitalGoodsStore.Goods>

    init {
        purchaseTable = object : VersionedEntitySqlTable<DigitalGoodsStore.Purchase>("purchase", PURCHASE, purchaseDbKeyFactory, dp) {
            override fun load(ctx: DSLContext, rs: Record): DigitalGoodsStore.Purchase {
                return SQLPurchase(rs)
            }

            override fun save(ctx: DSLContext, purchase: DigitalGoodsStore.Purchase) {
                savePurchase(ctx, purchase)
            }

            override fun defaultSort(): Collection<SortField<*>> {
                return listOf(tableClass.field("timestamp", Int::class.java).desc(), tableClass.field("id", Long::class.java).asc())
            }
        }

        feedbackTable = object : VersionedValuesSqlTable<DigitalGoodsStore.Purchase, EncryptedData>("purchase_feedback", PURCHASE_FEEDBACK, feedbackDbKeyFactory, dp) {

            override fun load(ctx: DSLContext, record: Record): EncryptedData {
                val data = record.get(PURCHASE_FEEDBACK.FEEDBACK_DATA)
                val nonce = record.get(PURCHASE_FEEDBACK.FEEDBACK_NONCE)
                return EncryptedData(data, nonce)
            }

            override fun save(ctx: DSLContext, purchase: DigitalGoodsStore.Purchase, encryptedData: EncryptedData) {
                var data: ByteArray? = null
                var nonce: ByteArray? = null
                if (encryptedData.data != null) {
                    data = encryptedData.data
                    nonce = encryptedData.nonce
                }
                ctx.insertInto<PurchaseFeedbackRecord, Long, ByteArray, ByteArray, Int, Boolean>(
                        PURCHASE_FEEDBACK,
                        PURCHASE_FEEDBACK.ID,
                        PURCHASE_FEEDBACK.FEEDBACK_DATA, PURCHASE_FEEDBACK.FEEDBACK_NONCE,
                        PURCHASE_FEEDBACK.HEIGHT, PURCHASE_FEEDBACK.LATEST
                ).values(
                        purchase.id,
                        data, nonce,
                        dp.blockchainService.height, true
                ).execute()
            }
        }

        publicFeedbackTable = object : VersionedValuesSqlTable<DigitalGoodsStore.Purchase, String>("purchase_public_feedback", PURCHASE_PUBLIC_FEEDBACK, publicFeedbackDbKeyFactory, dp) {

            override fun load(ctx: DSLContext, record: Record): String {
                return record.get(PURCHASE_PUBLIC_FEEDBACK.PUBLIC_FEEDBACK)
            }

            override fun save(ctx: DSLContext, purchase: DigitalGoodsStore.Purchase, publicFeedback: String) {
                ctx.mergeInto<PurchasePublicFeedbackRecord, Long, String, Int, Boolean>(PURCHASE_PUBLIC_FEEDBACK, PURCHASE_PUBLIC_FEEDBACK.ID, PURCHASE_PUBLIC_FEEDBACK.PUBLIC_FEEDBACK, PURCHASE_PUBLIC_FEEDBACK.HEIGHT, PURCHASE_PUBLIC_FEEDBACK.LATEST)
                        .key(PURCHASE_PUBLIC_FEEDBACK.ID, PURCHASE_PUBLIC_FEEDBACK.HEIGHT)
                        .values(purchase.id, publicFeedback, dp.blockchainService.height, true)
                        .execute()
            }
        }

        goodsTable = object : VersionedEntitySqlTable<DigitalGoodsStore.Goods>("goods", GOODS, goodsDbKeyFactory, dp) {

            override fun load(ctx: DSLContext, record: Record): DigitalGoodsStore.Goods {
                return SQLGoods(record)
            }

            override fun save(ctx: DSLContext, goods: DigitalGoodsStore.Goods) {
                saveGoods(ctx, goods)
            }

            override fun defaultSort(): Collection<SortField<*>> {
                return listOf(tableClass.field("timestamp", Int::class.java).desc(), tableClass.field("id", Long::class.java).asc())
            }
        }
    }

    override fun getExpiredPendingPurchases(timestamp: Int): Collection<DigitalGoodsStore.Purchase> {
        return purchaseTable.getManyBy(PURCHASE.DEADLINE.lt(timestamp).and(PURCHASE.PENDING.isTrue), 0, -1)
    }

    private fun loadEncryptedData(record: Record, dataField: Field<ByteArray>, nonceField: Field<ByteArray>): EncryptedData? {
        val data = record.get(dataField) ?: return null // TODO
        return EncryptedData(data, record.get(nonceField))
    }

    private fun saveGoods(ctx: DSLContext, goods: DigitalGoodsStore.Goods) {
        ctx.mergeInto<GoodsRecord, Long, Long, String, String, String, Int, Int, Long, Boolean, Int, Boolean>(GOODS, GOODS.ID, GOODS.SELLER_ID, GOODS.NAME, GOODS.DESCRIPTION, GOODS.TAGS, GOODS.TIMESTAMP, GOODS.QUANTITY, GOODS.PRICE, GOODS.DELISTED, GOODS.HEIGHT, GOODS.LATEST)
                .key(GOODS.ID, GOODS.HEIGHT)
                .values(goods.id, goods.sellerId, goods.name, goods.description, goods.tags, goods.timestamp, goods.quantity, goods.pricePlanck, goods.isDelisted, dp.blockchainService.height, true)
                .execute()
    }

    private fun savePurchase(ctx: DSLContext, purchase: DigitalGoodsStore.Purchase) {
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
        ctx.mergeInto<PurchaseRecord, Long, Long, Long, Long, Int, Long, Int, ByteArray, ByteArray, Int, Boolean, ByteArray, ByteArray, ByteArray, ByteArray, Boolean, Boolean, Long, Long, Int, Boolean>(PURCHASE, PURCHASE.ID, PURCHASE.BUYER_ID, PURCHASE.GOODS_ID, PURCHASE.SELLER_ID, PURCHASE.QUANTITY, PURCHASE.PRICE, PURCHASE.DEADLINE, PURCHASE.NOTE, PURCHASE.NONCE, PURCHASE.TIMESTAMP, PURCHASE.PENDING, PURCHASE.GOODS, PURCHASE.GOODS_NONCE, PURCHASE.REFUND_NOTE, PURCHASE.REFUND_NONCE, PURCHASE.HAS_FEEDBACK_NOTES, PURCHASE.HAS_PUBLIC_FEEDBACKS, PURCHASE.DISCOUNT, PURCHASE.REFUND, PURCHASE.HEIGHT, PURCHASE.LATEST)
                .key(PURCHASE.ID, PURCHASE.HEIGHT)
                .values(purchase.id, purchase.buyerId, purchase.goodsId, purchase.sellerId, purchase.quantity, purchase.pricePlanck, purchase.deliveryDeadlineTimestamp, note, nonce, purchase.timestamp, purchase.isPending, goods, goodsNonce, refundNote, refundNonce, purchase.feedbackNotes != null && purchase.feedbackNotes!!.isNotEmpty(),
                    purchase.getPublicFeedback()!!.isNotEmpty(), purchase.discountPlanck, purchase.refundPlanck, dp.blockchainService.height, true)
                .execute()
    }

    override fun getGoodsInStock(from: Int, to: Int): Collection<DigitalGoodsStore.Goods> {
        return goodsTable.getManyBy(GOODS.DELISTED.isFalse.and(GOODS.QUANTITY.gt(0)), from, to)
    }

    override fun getSellerGoods(sellerId: Long, inStockOnly: Boolean, from: Int, to: Int): Collection<DigitalGoodsStore.Goods> {
        return goodsTable.getManyBy(if (inStockOnly) GOODS.SELLER_ID.eq(sellerId).and(GOODS.DELISTED.isFalse).and(GOODS.QUANTITY.gt(0)) else GOODS.SELLER_ID.eq(sellerId), from, to, listOf(GOODS.field("name", String::class.java).asc(), GOODS.field("timestamp", Int::class.java).desc(), GOODS.field("id", Long::class.java).asc()))
    }

    override fun getAllPurchases(from: Int, to: Int): Collection<DigitalGoodsStore.Purchase> {
        return purchaseTable.getAll(from, to)
    }

    override fun getSellerPurchases(sellerId: Long, from: Int, to: Int): Collection<DigitalGoodsStore.Purchase> {
        return purchaseTable.getManyBy(PURCHASE.SELLER_ID.eq(sellerId), from, to)
    }

    override fun getBuyerPurchases(buyerId: Long, from: Int, to: Int): Collection<DigitalGoodsStore.Purchase> {
        return purchaseTable.getManyBy(PURCHASE.BUYER_ID.eq(buyerId), from, to)
    }

    override fun getSellerBuyerPurchases(sellerId: Long, buyerId: Long, from: Int, to: Int): Collection<DigitalGoodsStore.Purchase> {
        return purchaseTable.getManyBy(PURCHASE.SELLER_ID.eq(sellerId).and(PURCHASE.BUYER_ID.eq(buyerId)), from, to)
    }

    override fun getPendingSellerPurchases(sellerId: Long, from: Int, to: Int): Collection<DigitalGoodsStore.Purchase> {
        return purchaseTable.getManyBy(PURCHASE.SELLER_ID.eq(sellerId).and(PURCHASE.PENDING.isTrue), from, to)
    }

    private inner class SQLGoods internal constructor(record: Record) : DigitalGoodsStore.Goods(record.get(GOODS.ID), goodsDbKeyFactory.newKey(record.get(GOODS.ID)), record.get(GOODS.SELLER_ID), record.get(GOODS.NAME), record.get(GOODS.DESCRIPTION), record.get(GOODS.TAGS), record.get(GOODS.TIMESTAMP), record.get(GOODS.QUANTITY), record.get(GOODS.PRICE), record.get(GOODS.DELISTED))

    internal inner class SQLPurchase(record: Record) : DigitalGoodsStore.Purchase(dp, record.get(PURCHASE.ID), purchaseDbKeyFactory.newKey(record.get(PURCHASE.ID)), record.get(PURCHASE.BUYER_ID), record.get(PURCHASE.GOODS_ID), record.get(PURCHASE.SELLER_ID), record.get(PURCHASE.QUANTITY), record.get(PURCHASE.PRICE), record.get(PURCHASE.DEADLINE), loadEncryptedData(record, PURCHASE.NOTE, PURCHASE.NONCE), record.get(PURCHASE.TIMESTAMP), record.get(PURCHASE.PENDING), loadEncryptedData(record, PURCHASE.GOODS, PURCHASE.GOODS_NONCE), loadEncryptedData(record, PURCHASE.REFUND_NOTE, PURCHASE.REFUND_NONCE), record.get(PURCHASE.HAS_FEEDBACK_NOTES), record.get(PURCHASE.HAS_PUBLIC_FEEDBACKS), record.get(PURCHASE.DISCOUNT), record.get(PURCHASE.REFUND))
}
