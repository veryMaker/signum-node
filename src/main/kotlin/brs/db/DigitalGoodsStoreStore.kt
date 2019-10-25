package brs.db
import brs.entity.EncryptedData
import brs.entity.Goods
import brs.entity.Purchase

interface DigitalGoodsStoreStore {

    val feedbackDbKeyFactory: BurstKey.LongKeyFactory<Purchase>

    val purchaseDbKeyFactory: BurstKey.LongKeyFactory<Purchase>

    val purchaseTable: VersionedEntityTable<Purchase>

    val feedbackTable: VersionedValuesTable<Purchase, EncryptedData>

    val publicFeedbackDbKeyFactory: BurstKey.LongKeyFactory<Purchase>

    val publicFeedbackTable: VersionedValuesTable<Purchase, String>

    val goodsDbKeyFactory: BurstKey.LongKeyFactory<Goods>

    val goodsTable: VersionedEntityTable<Goods>

    fun getGoodsInStock(from: Int, to: Int): Collection<Goods>

    fun getSellerGoods(sellerId: Long, inStockOnly: Boolean, from: Int, to: Int): Collection<Goods>

    fun getAllPurchases(from: Int, to: Int): Collection<Purchase>

    fun getSellerPurchases(sellerId: Long, from: Int, to: Int): Collection<Purchase>

    fun getBuyerPurchases(buyerId: Long, from: Int, to: Int): Collection<Purchase>

    fun getSellerBuyerPurchases(sellerId: Long, buyerId: Long, from: Int, to: Int): Collection<Purchase>

    fun getPendingSellerPurchases(sellerId: Long, from: Int, to: Int): Collection<Purchase>

    fun getExpiredPendingPurchases(timestamp: Int): Collection<Purchase>
}
