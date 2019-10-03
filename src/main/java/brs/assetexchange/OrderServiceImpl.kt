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

    val bidCount: Int
        get() = bidOrderTable.count

    val askCount: Int
        get() = askOrderTable.count

    fun getAskOrder(orderId: Long): Ask?{
        return askOrderTable.get(askOrderDbKeyFactory.newKey(orderId))
    }

    fun getBidOrder(orderId: Long): Bid? {
        return bidOrderTable.get(bidOrderDbKeyFactory.newKey(orderId))
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

    suspend fun addAskOrder(transaction: Transaction, attachment: Attachment.ColoredCoinsAskOrderPlacement) {
        val dbKey = askOrderDbKeyFactory.newKey(transaction.id)
        val order = Ask(dbKey, transaction, attachment)
        askOrderTable.insert(order)
        matchOrders(attachment.assetId)
    }

    suspend fun addBidOrder(transaction: Transaction, attachment: Attachment.ColoredCoinsBidOrderPlacement) {
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

    private suspend fun matchOrders(assetId: Long) {
        var askOrder = getNextAskOrder(assetId)
        var bidOrder = getNextBidOrder(assetId)

        while (askOrder != null && bidOrder != null) {
            if (askOrder.priceNQT > bidOrder.priceNQT) break

            val trade = tradeService.addTrade(assetId, dp.blockchain.lastBlock, askOrder, bidOrder)

            askOrderUpdateQuantityQNT(askOrder, askOrder.quantityQNT.safeSubtract(trade.quantityQNT))
            val askAccount = dp.accountService.getAccount(askOrder.accountId)!!
            dp.accountService.addToBalanceAndUnconfirmedBalanceNQT(askAccount, trade.quantityQNT.safeMultiply(trade.priceNQT))
            dp.accountService.addToAssetBalanceQNT(askAccount, assetId, -trade.quantityQNT)

            bidOrderUpdateQuantityQNT(bidOrder, bidOrder.quantityQNT.safeSubtract(trade.quantityQNT))
            val bidAccount = dp.accountService.getAccount(bidOrder.accountId)!!
            dp.accountService.addToAssetAndUnconfirmedAssetBalanceQNT(bidAccount, assetId, trade.quantityQNT)
            dp.accountService.addToBalanceNQT(bidAccount, -trade.quantityQNT.safeMultiply(trade.priceNQT))
            dp.accountService.addToUnconfirmedBalanceNQT(bidAccount, trade.quantityQNT.safeMultiply(bidOrder.priceNQT - trade.priceNQT))

            askOrder = getNextAskOrder(assetId)
            bidOrder = getNextBidOrder(assetId)
        }
    }

    private fun askOrderUpdateQuantityQNT(askOrder: Ask, quantityQNT: Long) {
        askOrder.quantityQNT = quantityQNT
        when {
            quantityQNT > 0 -> askOrderTable.insert(askOrder)
            quantityQNT == 0L -> askOrderTable.delete(askOrder)
            else -> throw IllegalArgumentException("Negative quantity: " + quantityQNT + " for order: " + askOrder.id.toUnsignedString())
        }
    }

    private fun bidOrderUpdateQuantityQNT(bidOrder: Bid, quantityQNT: Long) {
        bidOrder.quantityQNT = quantityQNT
        when {
            quantityQNT > 0 -> bidOrderTable.insert(bidOrder)
            quantityQNT == 0L -> bidOrderTable.delete(bidOrder)
            else -> throw IllegalArgumentException("Negative quantity: " + quantityQNT + " for order: " + bidOrder.id.toUnsignedString())
        }
    }
}
