package brs.services

import brs.entity.Goods
import brs.entity.Purchase
import brs.entity.Transaction
import brs.transaction.appendix.Appendix
import brs.transaction.appendix.Attachment

interface DigitalGoodsStoreService {
    /**
     * TODO
     */
    enum class Event {
        GOODS_LISTED, GOODS_DELISTED, GOODS_PRICE_CHANGE, GOODS_QUANTITY_CHANGE,
        PURCHASE, DELIVERY, REFUND, FEEDBACK
    }

    /**
     * TODO
     */
    fun addGoodsListener(listener: (Goods) -> Unit, eventType: Event)

    /**
     * TODO
     */
    fun addPurchaseListener(listener: (Purchase) -> Unit, eventType: Event)

    /**
     * TODO
     */
    fun getGoods(goodsId: Long): Goods?

    /**
     * TODO
     */
    fun getAllGoods(from: Int, to: Int): Collection<Goods>

    /**
     * TODO
     */
    fun getGoodsInStock(from: Int, to: Int): Collection<Goods>

    /**
     * TODO
     */
    fun getSellerGoods(sellerId: Long, inStockOnly: Boolean, from: Int, to: Int): Collection<Goods>

    /**
     * TODO
     */
    fun getAllPurchases(from: Int, to: Int): Collection<Purchase>

    /**
     * TODO
     */
    fun getSellerPurchases(sellerId: Long, from: Int, to: Int): Collection<Purchase>

    /**
     * TODO
     */
    fun getBuyerPurchases(buyerId: Long, from: Int, to: Int): Collection<Purchase>

    /**
     * TODO
     */
    fun getSellerBuyerPurchases(sellerId: Long, buyerId: Long, from: Int, to: Int): Collection<Purchase>

    /**
     * TODO
     */
    fun getPendingSellerPurchases(sellerId: Long, from: Int, to: Int): Collection<Purchase>

    /**
     * TODO
     */
    fun getPurchase(purchaseId: Long): Purchase?

    /**
     * TODO
     */
    fun changeQuantity(goodsId: Long, deltaQuantity: Int, allowDelisted: Boolean)

    /**
     * TODO
     */
    fun purchase(transaction: Transaction, attachment: Attachment.DigitalGoodsPurchase)

    /**
     * TODO
     */
    fun addPurchase(transaction: Transaction, attachment: Attachment.DigitalGoodsPurchase, sellerId: Long)

    /**
     * TODO
     */
    fun listGoods(transaction: Transaction, attachment: Attachment.DigitalGoodsListing)

    /**
     * TODO
     */
    fun delistGoods(goodsId: Long)

    /**
     * TODO
     */
    fun feedback(purchaseId: Long, encryptedMessage: Appendix.EncryptedMessage?, message: Appendix.Message?)

    /**
     * TODO
     */
    fun refund(sellerId: Long, purchaseId: Long, refundPlanck: Long, encryptedMessage: Appendix.EncryptedMessage?)

    /**
     * TODO
     */
    fun getExpiredPendingPurchases(timestamp: Int): Collection<Purchase>

    /**
     * TODO
     */
    fun changePrice(goodsId: Long, pricePlanck: Long)

    /**
     * TODO
     */
    fun deliver(transaction: Transaction, attachment: Attachment.DigitalGoodsDelivery)

    /**
     * TODO
     */
    fun getPendingPurchase(purchaseId: Long): Purchase?

    /**
     * TODO
     */
    fun setPending(purchase: Purchase, pendingValue: Boolean)
}
