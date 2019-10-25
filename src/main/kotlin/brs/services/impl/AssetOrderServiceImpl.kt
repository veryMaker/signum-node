package brs.services.impl

import brs.DependencyProvider
import brs.entity.Order.Ask
import brs.entity.Order.Bid
import brs.services.AssetOrderService
import brs.entity.Transaction
import brs.transaction.appendix.Attachment
import brs.util.convert.safeMultiply
import brs.util.convert.safeSubtract
import brs.util.convert.toUnsignedString

internal class AssetOrderServiceImpl(private val dp: DependencyProvider, private val tradeService: AssetTradeServiceImpl) :
    AssetOrderService {
    override val askOrderTable = dp.orderStore.askOrderTable
    override val askOrderDbKeyFactory = dp.orderStore.askOrderDbKeyFactory
    override val bidOrderTable = dp.orderStore.bidOrderTable
    override val bidOrderDbKeyFactory = dp.orderStore.bidOrderDbKeyFactory

    override val bidCount get() = bidOrderTable.count

    override val askCount get() = askOrderTable.count

    override fun getAskOrder(orderId: Long): Ask?{
        return askOrderTable[askOrderDbKeyFactory.newKey(orderId)]
    }

    override fun getBidOrder(orderId: Long): Bid? {
        return bidOrderTable[bidOrderDbKeyFactory.newKey(orderId)]
    }

    override fun getAllAskOrders(from: Int, to: Int): Collection<Ask> {
        return askOrderTable.getAll(from, to)
    }

    override fun getAllBidOrders(from: Int, to: Int): Collection<Bid> {
        return bidOrderTable.getAll(from, to)
    }

    override fun getSortedBidOrders(assetId: Long, from: Int, to: Int): Collection<Bid> {
        return dp.orderStore.getSortedBids(assetId, from, to)
    }

    override fun getAskOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Ask> {
        return dp.orderStore.getAskOrdersByAccount(accountId, from, to)
    }

    override fun getAskOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Ask> {
        return dp.orderStore.getAskOrdersByAccountAsset(accountId, assetId, from, to)
    }

    override fun getSortedAskOrders(assetId: Long, from: Int, to: Int): Collection<Ask> {
        return dp.orderStore.getSortedAsks(assetId, from, to)
    }

    override fun getBidOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Bid> {
        return dp.orderStore.getBidOrdersByAccount(accountId, from, to)
    }

    override fun getBidOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Bid> {
        return dp.orderStore.getBidOrdersByAccountAsset(accountId, assetId, from, to)
    }

    override fun removeBidOrder(orderId: Long) {
        bidOrderTable.delete(getBidOrder(orderId) ?: return)
    }

    override fun removeAskOrder(orderId: Long) {
        askOrderTable.delete(getAskOrder(orderId) ?: return)
    }

    override fun addAskOrder(transaction: Transaction, attachment: Attachment.ColoredCoinsAskOrderPlacement) {
        val dbKey = askOrderDbKeyFactory.newKey(transaction.id)
        val order = Ask(dbKey, transaction, attachment)
        askOrderTable.insert(order)
        matchOrders(attachment.assetId)
    }

    override fun addBidOrder(transaction: Transaction, attachment: Attachment.ColoredCoinsBidOrderPlacement) {
        val dbKey = bidOrderDbKeyFactory.newKey(transaction.id)
        val order = Bid(dbKey, transaction, attachment)
        bidOrderTable.insert(order)
        matchOrders(attachment.assetId)
    }

    override fun getNextAskOrder(assetId: Long): Ask? {
        return dp.orderStore.getNextOrder(assetId)
    }

    override fun getNextBidOrder(assetId: Long): Bid? {
        return dp.orderStore.getNextBid(assetId)
    }

    override fun matchOrders(assetId: Long) {
        var askOrder = getNextAskOrder(assetId)
        var bidOrder = getNextBidOrder(assetId)

        while (askOrder != null && bidOrder != null) {
            if (askOrder.pricePlanck > bidOrder.pricePlanck) break

            val trade = tradeService.addTrade(assetId, dp.blockchainService.lastBlock, askOrder, bidOrder)

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

    override fun askOrderUpdateQuantity(askOrder: Ask, quantity: Long) {
        askOrder.quantity = quantity
        when {
            quantity > 0 -> askOrderTable.insert(askOrder)
            quantity == 0L -> askOrderTable.delete(askOrder)
            else -> throw IllegalArgumentException("Negative quantity: " + quantity + " for order: " + askOrder.id.toUnsignedString())
        }
    }

    override fun bidOrderUpdateQuantity(bidOrder: Bid, quantity: Long) {
        bidOrder.quantity = quantity
        when {
            quantity > 0 -> bidOrderTable.insert(bidOrder)
            quantity == 0L -> bidOrderTable.delete(bidOrder)
            else -> throw IllegalArgumentException("Negative quantity: " + quantity + " for order: " + bidOrder.id.toUnsignedString())
        }
    }
}
