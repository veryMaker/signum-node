package brs.services.impl

import brs.entity.*
import brs.services.DigitalGoodsStoreService
import brs.services.DigitalGoodsStoreService.Event
import brs.transaction.appendix.Appendix
import brs.transaction.appendix.Attachment
import brs.util.Listeners
import brs.util.convert.safeMultiply
import brs.util.convert.safeSubtract
import brs.util.convert.toUtf8String

class DigitalGoodsStoreServiceImpl(private val dp: DependencyProvider) : DigitalGoodsStoreService {
    private val feedbackTable = dp.digitalGoodsStoreStore.feedbackTable
    private val publicFeedbackTable = dp.digitalGoodsStoreStore.publicFeedbackTable

    private val goodsTable = dp.digitalGoodsStoreStore.goodsTable
    private val purchaseTable = dp.digitalGoodsStoreStore.purchaseTable
    private val goodsDbKeyFactory = dp.digitalGoodsStoreStore.goodsDbKeyFactory
    private val purchaseDbKeyFactory = dp.digitalGoodsStoreStore.purchaseDbKeyFactory

    private val goodsListeners = Listeners<Goods, Event>()
    private val purchaseListeners = Listeners<Purchase, Event>()

    override fun addGoodsListener(listener: (Goods) -> Unit, eventType: Event) {
        goodsListeners.addListener(eventType, listener)
    }

    override fun addPurchaseListener(listener: (Purchase) -> Unit, eventType: Event) {
        purchaseListeners.addListener(eventType, listener)
    }

    override fun getGoods(goodsId: Long): Goods? {
        return goodsTable[goodsDbKeyFactory.newKey(goodsId)]
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
        return purchaseTable[purchaseDbKeyFactory.newKey(purchaseId)]
    }

    override fun changeQuantity(goodsId: Long, deltaQuantity: Int, allowDelisted: Boolean) {
        val goods = goodsTable[goodsDbKeyFactory.newKey(goodsId)]!!
        if (allowDelisted || !goods.isDelisted) {
            goods.changeQuantity(deltaQuantity)
            goodsTable.insert(goods)
            goodsListeners.accept(Event.GOODS_QUANTITY_CHANGE, goods)
        } else {
            error("Can't change quantity of delisted goods")
        }
    }

    override fun purchase(transaction: Transaction, attachment: Attachment.DigitalGoodsPurchase) {
        val goods = goodsTable[goodsDbKeyFactory.newKey(attachment.goodsId)]!!
        if (!goods.isDelisted && attachment.quantity <= goods.quantity && attachment.pricePlanck == goods.pricePlanck
            && attachment.deliveryDeadlineTimestamp > dp.blockchainService.lastBlock.timestamp
        ) {
            changeQuantity(goods.id, -attachment.quantity, false)
            addPurchase(transaction, attachment, goods.sellerId)
        } else {
            val buyer = dp.accountService.getAccount(transaction.senderId)!!
            dp.accountService.addToUnconfirmedBalancePlanck(
                buyer,
                attachment.quantity.toLong().safeMultiply(attachment.pricePlanck)
            )
            // restoring the unconfirmed balance if purchase not successful, however buyer still lost the transaction fees
        }
    }

    override fun addPurchase(transaction: Transaction, attachment: Attachment.DigitalGoodsPurchase, sellerId: Long) {
        val purchase = Purchase(dp, transaction, attachment, sellerId)
        purchaseTable.insert(purchase)
        purchaseListeners.accept(Event.PURCHASE, purchase)
    }

    override fun listGoods(transaction: Transaction, attachment: Attachment.DigitalGoodsListing) {
        val dbKey = goodsDbKeyFactory.newKey(transaction.id)
        val goods = Goods(dbKey, transaction, attachment)
        goodsTable.insert(goods)
        goodsListeners.accept(Event.GOODS_LISTED, goods)
    }

    override fun delistGoods(goodsId: Long) {
        val goods = goodsTable[goodsDbKeyFactory.newKey(goodsId)]!!
        if (!goods.isDelisted) {
            goods.isDelisted = true
            goodsTable.insert(goods)
            goodsListeners.accept(Event.GOODS_DELISTED, goods)
        } else {
            error("Goods already delisted")
        }
    }

    override fun feedback(purchaseId: Long, encryptedMessage: Appendix.EncryptedMessage?, message: Appendix.Message?) {
        val purchase = purchaseTable[purchaseDbKeyFactory.newKey(purchaseId)]!!
        if (encryptedMessage != null) {
            purchase.addFeedbackNote(encryptedMessage.encryptedData)
            purchaseTable.insert(purchase)
            feedbackTable.insert(purchase, purchase.feedbackNotes!!)
        }
        if (message != null) {
            addPublicFeedback(purchase, message.messageBytes.toUtf8String())
        }
        purchaseListeners.accept(Event.FEEDBACK, purchase)
    }

    private fun addPublicFeedback(purchase: Purchase, publicFeedback: String) {
        var publicFeedbacks: MutableList<String>? = purchase.publicFeedbacks
        if (publicFeedbacks == null) {
            publicFeedbacks = mutableListOf()
        }
        publicFeedbacks.add(publicFeedback)
        purchase.setHasPublicFeedbacks(true)
        purchaseTable.insert(purchase)
        publicFeedbackTable.insert(purchase, publicFeedbacks)
    }

    override fun refund(
        sellerId: Long,
        purchaseId: Long,
        refundPlanck: Long,
        encryptedMessage: Appendix.EncryptedMessage?
    ) {
        val purchase = purchaseTable[purchaseDbKeyFactory.newKey(purchaseId)]!!
        val seller = dp.accountService.getAccount(sellerId)!!
        dp.accountService.addToBalancePlanck(seller, -refundPlanck)
        val buyer = dp.accountService.getAccount(purchase.buyerId)!!
        dp.accountService.addToBalanceAndUnconfirmedBalancePlanck(buyer, refundPlanck)
        if (encryptedMessage != null) {
            purchase.refundNote = encryptedMessage.encryptedData
            purchaseTable.insert(purchase)
        }
        purchase.refundPlanck = refundPlanck
        purchaseTable.insert(purchase)
        purchaseListeners.accept(Event.REFUND, purchase)
    }

    override fun getExpiredPendingPurchases(timestamp: Int): Collection<Purchase> {
        return dp.digitalGoodsStoreStore.getExpiredPendingPurchases(timestamp)
    }

    override fun changePrice(goodsId: Long, pricePlanck: Long) {
        val goods = goodsTable[goodsDbKeyFactory.newKey(goodsId)]!!
        if (!goods.isDelisted) {
            goods.changePrice(pricePlanck)
            goodsTable.insert(goods)
            goodsListeners.accept(Event.GOODS_PRICE_CHANGE, goods)
        } else {
            error("Can't change price of delisted goods")
        }
    }

    override fun deliver(transaction: Transaction, attachment: Attachment.DigitalGoodsDelivery) {
        val purchase = getPendingPurchase(attachment.purchaseId)
            ?: throw RuntimeException("cant find purchase with id " + attachment.purchaseId)
        setPending(purchase, false)
        val totalWithoutDiscount = purchase.quantity.toLong().safeMultiply(purchase.pricePlanck)
        val buyer = dp.accountService.getAccount(purchase.buyerId)!!
        dp.accountService.addToBalancePlanck(buyer, attachment.discountPlanck.safeSubtract(totalWithoutDiscount))
        dp.accountService.addToUnconfirmedBalancePlanck(buyer, attachment.discountPlanck)
        val seller = dp.accountService.getAccount(transaction.senderId)!!
        dp.accountService.addToBalanceAndUnconfirmedBalancePlanck(
            seller,
            totalWithoutDiscount.safeSubtract(attachment.discountPlanck)
        )
        purchase.setEncryptedGoods(attachment.goods, attachment.goodsIsText())
        purchaseTable.insert(purchase)
        purchase.discountPlanck = attachment.discountPlanck
        purchaseTable.insert(purchase)
        purchaseListeners.accept(Event.DELIVERY, purchase)
    }

    override fun getPendingPurchase(purchaseId: Long): Purchase? {
        val purchase = getPurchase(purchaseId)
        return if (purchase == null || !purchase.isPending) null else purchase
    }

    override fun setPending(purchase: Purchase, pendingValue: Boolean) {
        purchase.isPending = pendingValue
        purchaseTable.insert(purchase)
    }

    companion object {
        fun expiredPurchaseListener(dp: DependencyProvider): (Block) -> Unit = { block ->
            for (purchase in dp.digitalGoodsStoreService.getExpiredPendingPurchases(block.timestamp)) {
                val buyer = dp.accountService.getAccount(purchase.buyerId)!!
                dp.accountService.addToUnconfirmedBalancePlanck(
                    buyer,
                    purchase.quantity.toLong().safeMultiply(purchase.pricePlanck)
                )
                dp.digitalGoodsStoreService.changeQuantity(purchase.goodsId, purchase.quantity, true)
                dp.digitalGoodsStoreService.setPending(purchase, false)
            }
        }
    }
}
