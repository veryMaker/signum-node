package brs.services.impl

import brs.*
import brs.DigitalGoodsStore.Event
import brs.DigitalGoodsStore.Goods
import brs.DigitalGoodsStore.Purchase
import brs.crypto.EncryptedData
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.VersionedEntityTable
import brs.db.VersionedValuesTable
import brs.db.store.DigitalGoodsStoreStore
import brs.services.AccountService
import brs.services.DGSGoodsStoreService
import brs.util.Convert
import brs.util.Listeners

import java.util.ArrayList
import java.util.function.Consumer

class DGSGoodsStoreServiceImpl(private val dp: DependencyProvider) : DGSGoodsStoreService {
    private val feedbackTable: VersionedValuesTable<Purchase, EncryptedData>
    private val publicFeedbackTable: VersionedValuesTable<Purchase, String>

    private val goodsTable: VersionedEntityTable<Goods>
    private val purchaseTable: VersionedEntityTable<Purchase>
    private val goodsDbKeyFactory: LongKeyFactory<Goods>
    private val purchaseDbKeyFactory: LongKeyFactory<Purchase>

    private val goodsListeners = Listeners<Goods, Event>()

    private val purchaseListeners = Listeners<Purchase, Event>()

    init {
        this.goodsTable = dp.digitalGoodsStoreStore.goodsTable
        this.purchaseTable = dp.digitalGoodsStoreStore.purchaseTable
        this.goodsDbKeyFactory = dp.digitalGoodsStoreStore.goodsDbKeyFactory
        this.purchaseDbKeyFactory = dp.digitalGoodsStoreStore.purchaseDbKeyFactory
        this.feedbackTable = dp.digitalGoodsStoreStore.feedbackTable
        this.publicFeedbackTable = dp.digitalGoodsStoreStore.publicFeedbackTable
    }

    override fun addGoodsListener(listener: Consumer<Goods>, eventType: Event): Boolean {
        return goodsListeners.addListener(listener, eventType)
    }

    override fun removeGoodsListener(listener: Consumer<Goods>, eventType: Event): Boolean {
        return goodsListeners.removeListener(listener, eventType)
    }

    override fun addPurchaseListener(listener: Consumer<Purchase>, eventType: Event): Boolean {
        return purchaseListeners.addListener(listener, eventType)
    }

    override fun removePurchaseListener(listener: Consumer<Purchase>, eventType: Event): Boolean {
        return purchaseListeners.removeListener(listener, eventType)
    }

    override fun getGoods(goodsId: Long): Goods {
        return goodsTable.get(goodsDbKeyFactory.newKey(goodsId))
    }

    override fun getAllGoods(from: Int, to: Int): Collection<Goods> {
        return goodsTable.getAll(from, to)
    }

    override fun getGoodsInStock(from: Int, to: Int): Collection<Goods> {
        return dp.digitalGoodsStoreStore.getGoodsInStock(from, to)
    }

    override fun getSellerGoods(sellerId: Long, inStockOnly: Boolean, from: Int, to: Int): Collection<Goods> {
        return dp.digitalGoodsStoreStore.getSellerGoods(sellerId, inStockOnly, from, to)
    }

    override fun getAllPurchases(from: Int, to: Int): Collection<Purchase> {
        return purchaseTable.getAll(from, to)
    }

    override fun getSellerPurchases(sellerId: Long, from: Int, to: Int): Collection<Purchase> {
        return dp.digitalGoodsStoreStore.getSellerPurchases(sellerId, from, to)
    }

    override fun getBuyerPurchases(buyerId: Long, from: Int, to: Int): Collection<Purchase> {
        return dp.digitalGoodsStoreStore.getBuyerPurchases(buyerId, from, to)
    }

    override fun getSellerBuyerPurchases(sellerId: Long, buyerId: Long, from: Int, to: Int): Collection<Purchase> {
        return dp.digitalGoodsStoreStore.getSellerBuyerPurchases(sellerId, buyerId, from, to)
    }

    override fun getPendingSellerPurchases(sellerId: Long, from: Int, to: Int): Collection<Purchase> {
        return dp.digitalGoodsStoreStore.getPendingSellerPurchases(sellerId, from, to)
    }

    override fun getPurchase(purchaseId: Long): Purchase? {
        return purchaseTable.get(purchaseDbKeyFactory.newKey(purchaseId))
    }

    override fun changeQuantity(goodsId: Long, deltaQuantity: Int, allowDelisted: Boolean) {
        val goods = goodsTable.get(goodsDbKeyFactory.newKey(goodsId))
        if (allowDelisted || !goods.isDelisted) {
            goods.changeQuantity(deltaQuantity)
            goodsTable.insert(goods)
            goodsListeners.accept(goods, Event.GOODS_QUANTITY_CHANGE)
        } else {
            throw IllegalStateException("Can't change quantity of delisted goods")
        }
    }

    override fun purchase(transaction: Transaction, attachment: Attachment.DigitalGoodsPurchase) {
        val goods = goodsTable.get(goodsDbKeyFactory.newKey(attachment.goodsId))
        if (!goods.isDelisted && attachment.quantity <= goods.quantity && attachment.priceNQT == goods.priceNQT
                && attachment.deliveryDeadlineTimestamp > dp.blockchain.lastBlock.timestamp) {
            changeQuantity(goods.id, -attachment.quantity, false)
            addPurchase(transaction, attachment, goods.sellerId)
        } else {
            val buyer = dp.accountService.getAccount(transaction.senderId)
            dp.accountService.addToUnconfirmedBalanceNQT(buyer, Convert.safeMultiply(attachment.quantity.toLong(), attachment.priceNQT))
            // restoring the unconfirmed balance if purchase not successful, however buyer still lost the transaction fees
        }
    }

    override fun addPurchase(transaction: Transaction, attachment: Attachment.DigitalGoodsPurchase, sellerId: Long) {
        val purchase = Purchase(dp, transaction, attachment, sellerId)
        purchaseTable.insert(purchase)
        purchaseListeners.accept(purchase, Event.PURCHASE)
    }

    override fun listGoods(transaction: Transaction, attachment: Attachment.DigitalGoodsListing) {
        val dbKey = goodsDbKeyFactory.newKey(transaction.id)
        val goods = Goods(dbKey, transaction, attachment)
        goodsTable.insert(goods)
        goodsListeners.accept(goods, Event.GOODS_LISTED)
    }

    override fun delistGoods(goodsId: Long) {
        val goods = goodsTable.get(goodsDbKeyFactory.newKey(goodsId))
        if (!goods.isDelisted) {
            goods.isDelisted = true
            goodsTable.insert(goods)
            goodsListeners.accept(goods, Event.GOODS_DELISTED)
        } else {
            throw IllegalStateException("Goods already delisted")
        }
    }

    override fun feedback(purchaseId: Long, encryptedMessage: Appendix.EncryptedMessage?, message: Appendix.Message?) {
        val purchase = purchaseTable.get(purchaseDbKeyFactory.newKey(purchaseId))
        if (encryptedMessage != null) {
            purchase.addFeedbackNote(encryptedMessage.encryptedData)
            purchaseTable.insert(purchase)
            feedbackTable.insert(purchase, purchase.feedbackNotes)
        }
        if (message != null) {
            addPublicFeedback(purchase, Convert.toString(message.messageBytes!!))
        }
        purchaseListeners.accept(purchase, Event.FEEDBACK)
    }

    private fun addPublicFeedback(purchase: Purchase, publicFeedback: String) {
        var publicFeedbacks: MutableList<String>? = purchase.publicFeedbacks
        if (publicFeedbacks == null) {
            publicFeedbacks = ArrayList()
        }
        publicFeedbacks.add(publicFeedback)
        purchase.setHasPublicFeedbacks(true)
        purchaseTable.insert(purchase)
        publicFeedbackTable.insert(purchase, publicFeedbacks)
    }

    override fun refund(sellerId: Long, purchaseId: Long, refundNQT: Long, encryptedMessage: Appendix.EncryptedMessage?) {
        val purchase = purchaseTable.get(purchaseDbKeyFactory.newKey(purchaseId))
        val seller = dp.accountService.getAccount(sellerId)
        dp.accountService.addToBalanceNQT(seller, -refundNQT)
        val buyer = dp.accountService.getAccount(purchase.buyerId)
        dp.accountService.addToBalanceAndUnconfirmedBalanceNQT(buyer, refundNQT)
        if (encryptedMessage != null) {
            purchase.refundNote = encryptedMessage.encryptedData
            purchaseTable.insert(purchase)
        }
        purchase.refundNQT = refundNQT
        purchaseTable.insert(purchase)
        purchaseListeners.accept(purchase, Event.REFUND)
    }

    override fun getExpiredPendingPurchases(timestamp: Int): Collection<Purchase> {
        return dp.digitalGoodsStoreStore.getExpiredPendingPurchases(timestamp)
    }

    override fun changePrice(goodsId: Long, priceNQT: Long) {
        val goods = goodsTable.get(goodsDbKeyFactory.newKey(goodsId))
        if (!goods.isDelisted) {
            goods.changePrice(priceNQT)
            goodsTable.insert(goods)
            goodsListeners.accept(goods, Event.GOODS_PRICE_CHANGE)
        } else {
            throw IllegalStateException("Can't change price of delisted goods")
        }
    }

    override fun deliver(transaction: Transaction, attachment: Attachment.DigitalGoodsDelivery) {
        val purchase = getPendingPurchase(attachment.purchaseId)
                ?: throw RuntimeException("cant find purchase with id " + attachment.purchaseId)
        setPending(purchase, false)
        val totalWithoutDiscount = Convert.safeMultiply(purchase.quantity.toLong(), purchase.priceNQT)
        val buyer = dp.accountService.getAccount(purchase.buyerId)
        dp.accountService.addToBalanceNQT(buyer, Convert.safeSubtract(attachment.discountNQT, totalWithoutDiscount))
        dp.accountService.addToUnconfirmedBalanceNQT(buyer, attachment.discountNQT)
        val seller = dp.accountService.getAccount(transaction.senderId)
        dp.accountService.addToBalanceAndUnconfirmedBalanceNQT(seller, Convert.safeSubtract(totalWithoutDiscount, attachment.discountNQT))
        purchase.setEncryptedGoods(attachment.goods, attachment.goodsIsText())
        purchaseTable.insert(purchase)
        purchase.discountNQT = attachment.discountNQT
        purchaseTable.insert(purchase)
        purchaseListeners.accept(purchase, Event.DELIVERY)
    }

    override fun getPendingPurchase(purchaseId: Long): Purchase? {
        val purchase = getPurchase(purchaseId)
        return if (purchase == null || !purchase.isPending) null else purchase
    }

    override fun setPending(purchase: Purchase, pendingValue: Boolean) {
        purchase.isPending = pendingValue
        purchaseTable.insert(purchase)
    }

    class ExpiredPurchaseListener(private val dp: DependencyProvider) : Consumer<Block> {

        override fun accept(block: Block) {
            for (purchase in dp.digitalGoodsStoreService.getExpiredPendingPurchases(block.timestamp)) {
                val buyer = dp.accountService.getAccount(purchase.buyerId)
                dp.accountService.addToUnconfirmedBalanceNQT(buyer, Convert.safeMultiply(purchase.quantity.toLong(), purchase.priceNQT))
                dp.digitalGoodsStoreService.changeQuantity(purchase.goodsId, purchase.quantity, true)
                dp.digitalGoodsStoreService.setPending(purchase, false)
            }
        }
    }
}
