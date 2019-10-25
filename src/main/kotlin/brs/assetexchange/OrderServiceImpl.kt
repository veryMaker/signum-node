package brs.assetexchange

import brs.Attachment
import brs.DependencyProvider
import brs.Order.Ask
import brs.Order.Bid
import brs.Transaction
import brs.util.convert.safeMultiply
import brs.util.convert.safeSubtract
import brs.util.convert.toUnsignedString

internal class OrderServiceImpl(private val dp: DependencyProvider, private val tradeService: TradeServiceImpl) { // TODO interface
    private val askOrderTable = dp.orderStore.askOrderTable
    private val askOrderDbKeyFactory = dp.orderStore.askOrderDbKeyFactory
    private val bidOrderTable = dp.orderStore.bidOrderTable
    private val bidOrderDbKeyFactory = dp.orderStore.bidOrderDbKeyFactory

    val bidCount get() = bidOrderTable.count

    val askCount get() = askOrderTable.count

    fun getAskOrder(orderId: Long): Ask?{
        return askOrderTable[askOrderDbKeyFactory.newKey(orderId)]
    }

    fun getBidOrder(orderId: Long): Bid? {
        return bidOrderTable[bidOrderDbKeyFactory.newKey(orderId)]
    }

    fun getAllAskOrders(from: Int, to: Int): Collection<Ask> {
        return askOrderTable.getAll(from, to)
    }

    fun getAllBidOrders(from: Int, to: Int): Collection<Bid> {
        return bidOrderTable.getAll(from, to)
    }

    fun getSortedBidOrders(assetId: Long, from: Int, to: Int): Collection<Bid> {
        return dp.orderStore.getSortedBids(assetId, from, to)
    }

    fun getAskOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Ask> {
        return dp.orderStore.getAskOrdersByAccount(accountId, from, to)
    }

    fun getAskOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Ask> {
        return dp.orderStore.getAskOrdersByAccountAsset(accountId, assetId, from, to)
    }

    fun getSortedAskOrders(assetId: Long, from: Int, to: Int): Collection<Ask> {
        return dp.orderStore.getSortedAsks(assetId, from, to)
    }

    fun getBidOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Bid> {
        return dp.orderStore.getBidOrdersByAccount(accountId, from, to)
    }

    fun getBidOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Bid> {
        return dp.orderStore.getBidOrdersByAccountAsset(accountId, assetId, from, to)
    }

    fun removeBidOrder(orderId: Long) {
        bidOrderTable.delete(getBidOrder(orderId) ?: return)
    }

    fun removeAskOrder(orderId: Long) {
        askOrderTable.delete(getAskOrder(orderId) ?: return)
    }

    fun addAskOrder(transaction: Transaction, attachment: Attachment.ColoredCoinsAskOrderPlacement) {
        val dbKey = askOrderDbKeyFactory.newKey(transaction.id)
        val order = Ask(dbKey, transaction, attachment)
        askOrderTable.insert(order)
        matchOrders(attachment.assetId)
    }

    fun addBidOrder(transaction: Transaction, attachment: Attachment.ColoredCoinsBidOrderPlacement) {
        val dbKey = bidOrderDbKeyFactory.newKey(transaction.id)
        val order = Bid(dbKey, transaction, attachment)
        bidOrderTable.insert(order)
        matchOrders(attachment.assetId)
    }

    private fun getNextAskOrder(assetId: Long): Ask? {
        return dp.orderStore.getNextOrder(assetId)
    }

    private fun getNextBidOrder(assetId: Long): Bid? {
        return dp.orderStore.getNextBid(assetId)
    }

    private fun matchOrders(assetId: Long) {
        var askOrder = getNextAskOrder(assetId)
        var bidOrder = getNextBidOrder(assetId)

        while (askOrder != null && bidOrder != null) {
            if (askOrder.pricePlanck > bidOrder.pricePlanck) break

            val trade = tradeService.addTrade(assetId, dp.blockchain.lastBlock, askOrder, bidOrder)

            askOrderUpdateQuantity(askOrder, askOrder.quantity.safeSubtract(trade.quantity))
            val askAccount = dp.accountService.getAccount(askOrder.accountId)!!
            dp.accountService.addToBalanceAndUnconfirmedBalancePlanck(askAccount, trade.quantity.safeMultiply(trade.pricePlanck))
            dp.accountService.addToAssetBalanceQuantity(askAccount, assetId, -trade.quantity)

            bidOrderUpdateQuantity(bidOrder, bidOrder.quantity.safeSubtract(trade.quantity))
            val bidAccount = dp.accountService.getAccount(bidOrder.accountId)!!
            dp.accountService.addToAssetAndUnconfirmedAssetBalanceQuantity(bidAccount, assetId, trade.quantity)
            dp.accountService.addToBalancePlanck(bidAccount, -trade.quantity.safeMultiply(trade.pricePlanck))
            dp.accountService.addToUnconfirmedBalancePlanck(bidAccount, trade.quantity.safeMultiply(bidOrder.pricePlanck - trade.pricePlanck))

            askOrder = getNextAskOrder(assetId)
            bidOrder = getNextBidOrder(assetId)
        }
    }

    private fun askOrderUpdateQuantity(askOrder: Ask, quantity: Long) {
        askOrder.quantity = quantity
        when {
            quantity > 0 -> askOrderTable.insert(askOrder)
            quantity == 0L -> askOrderTable.delete(askOrder)
            else -> throw IllegalArgumentException("Negative quantity: " + quantity + " for order: " + askOrder.id.toUnsignedString())
        }
    }

    private fun bidOrderUpdateQuantity(bidOrder: Bid, quantity: Long) {
        bidOrder.quantity = quantity
        when {
            quantity > 0 -> bidOrderTable.insert(bidOrder)
            quantity == 0L -> bidOrderTable.delete(bidOrder)
            else -> throw IllegalArgumentException("Negative quantity: " + quantity + " for order: " + bidOrder.id.toUnsignedString())
        }
    }
}
