package brs.services

import brs.Appendix
import brs.Attachment
import brs.DigitalGoodsStore.Event
import brs.DigitalGoodsStore.Goods
import brs.DigitalGoodsStore.Purchase
import brs.Transaction

interface DGSGoodsStoreService { // TODO Redundant name!
    suspend fun addGoodsListener(listener: suspend (Goods) -> Unit, eventType: Event)

    suspend fun addPurchaseListener(listener: suspend (Purchase) -> Unit, eventType: Event)

    suspend fun getGoods(goodsId: Long): Goods?

    suspend fun getAllGoods(from: Int, to: Int): Collection<Goods>

    suspend fun getGoodsInStock(from: Int, to: Int): Collection<Goods>

    suspend fun getSellerGoods(sellerId: Long, inStockOnly: Boolean, from: Int, to: Int): Collection<Goods>

    suspend fun getAllPurchases(from: Int, to: Int): Collection<Purchase>

    suspend fun getSellerPurchases(sellerId: Long, from: Int, to: Int): Collection<Purchase>

    suspend fun getBuyerPurchases(buyerId: Long, from: Int, to: Int): Collection<Purchase>

    suspend fun getSellerBuyerPurchases(sellerId: Long, buyerId: Long, from: Int, to: Int): Collection<Purchase>

    suspend fun getPendingSellerPurchases(sellerId: Long, from: Int, to: Int): Collection<Purchase>

    suspend fun getPurchase(purchaseId: Long): Purchase?

    suspend fun changeQuantity(goodsId: Long, deltaQuantity: Int, allowDelisted: Boolean)

    suspend fun purchase(transaction: Transaction, attachment: Attachment.DigitalGoodsPurchase)

    suspend fun addPurchase(transaction: Transaction, attachment: Attachment.DigitalGoodsPurchase, sellerId: Long)

    suspend fun listGoods(transaction: Transaction, attachment: Attachment.DigitalGoodsListing)

    suspend fun delistGoods(goodsId: Long)

    suspend fun feedback(purchaseId: Long, encryptedMessage: Appendix.EncryptedMessage?, message: Appendix.Message?)

    suspend fun refund(sellerId: Long, purchaseId: Long, refundNQT: Long, encryptedMessage: Appendix.EncryptedMessage?)

    suspend fun getExpiredPendingPurchases(timestamp: Int): Collection<Purchase>

    suspend fun changePrice(goodsId: Long, priceNQT: Long)

    suspend fun deliver(transaction: Transaction, attachment: Attachment.DigitalGoodsDelivery)

    suspend fun getPendingPurchase(purchaseId: Long): Purchase?

    suspend fun setPending(purchase: Purchase, pendingValue: Boolean)
}
