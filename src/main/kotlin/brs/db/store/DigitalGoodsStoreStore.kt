package brs.db.store

import brs.DigitalGoodsStore
import brs.crypto.EncryptedData
import brs.db.BurstKey
import brs.db.VersionedEntityTable
import brs.db.VersionedValuesTable

interface DigitalGoodsStoreStore {

    val feedbackDbKeyFactory: BurstKey.LongKeyFactory<DigitalGoodsStore.Purchase>

    val purchaseDbKeyFactory: BurstKey.LongKeyFactory<DigitalGoodsStore.Purchase>

    val purchaseTable: VersionedEntityTable<DigitalGoodsStore.Purchase>

    val feedbackTable: VersionedValuesTable<DigitalGoodsStore.Purchase, EncryptedData>

    val publicFeedbackDbKeyFactory: BurstKey.LongKeyFactory<DigitalGoodsStore.Purchase>

    val publicFeedbackTable: VersionedValuesTable<DigitalGoodsStore.Purchase, String>

    val goodsDbKeyFactory: BurstKey.LongKeyFactory<DigitalGoodsStore.Goods>

    val goodsTable: VersionedEntityTable<DigitalGoodsStore.Goods>

    fun getGoodsInStock(from: Int, to: Int): Collection<DigitalGoodsStore.Goods>

    fun getSellerGoods(sellerId: Long, inStockOnly: Boolean, from: Int, to: Int): Collection<DigitalGoodsStore.Goods>

    fun getAllPurchases(from: Int, to: Int): Collection<DigitalGoodsStore.Purchase>

    fun getSellerPurchases(sellerId: Long, from: Int, to: Int): Collection<DigitalGoodsStore.Purchase>

    fun getBuyerPurchases(buyerId: Long, from: Int, to: Int): Collection<DigitalGoodsStore.Purchase>

    fun getSellerBuyerPurchases(sellerId: Long, buyerId: Long, from: Int, to: Int): Collection<DigitalGoodsStore.Purchase>

    fun getPendingSellerPurchases(sellerId: Long, from: Int, to: Int): Collection<DigitalGoodsStore.Purchase>

    fun getExpiredPendingPurchases(timestamp: Int): Collection<DigitalGoodsStore.Purchase>
}
