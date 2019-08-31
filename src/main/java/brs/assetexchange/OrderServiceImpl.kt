package brs.assetexchange

import brs.*
import brs.Order.Ask
import brs.Order.Bid
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.VersionedEntityTable
import brs.db.store.OrderStore
import brs.services.AccountService
import brs.util.Convert

internal class OrderServiceImpl(private val dp: DependencyProvider, private val tradeService: TradeServiceImpl) {
    private val askOrderTable: VersionedEntityTable<Ask>
    private val askOrderDbKeyFactory: LongKeyFactory<Ask>
    private val bidOrderTable: VersionedEntityTable<Bid>
    private val bidOrderDbKeyFactory: LongKeyFactory<Bid>

    val bidCount: Int
        get() = bidOrderTable.count

    val askCount: Int
        get() = askOrderTable.count

    init {
        this.askOrderTable = dp.orderStore.askOrderTable
        this.askOrderDbKeyFactory = dp.orderStore.askOrderDbKeyFactory
        this.bidOrderTable = dp.orderStore.bidOrderTable
        this.bidOrderDbKeyFactory = dp.orderStore.bidOrderDbKeyFactory
    }

    fun getAskOrder(orderId: Long): Ask {
        return askOrderTable.get(askOrderDbKeyFactory.newKey(orderId))
    }

    fun getBidOrder(orderId: Long): Bid {
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
        bidOrderTable.delete(getBidOrder(orderId))
    }

    fun removeAskOrder(orderId: Long) {
        askOrderTable.delete(getAskOrder(orderId))
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

    private fun getNextAskOrder(assetId: Long): Ask {
        return dp.orderStore.getNextOrder(assetId)
    }

    private fun getNextBidOrder(assetId: Long): Bid {
        return dp.orderStore.getNextBid(assetId)
    }

    private fun matchOrders(assetId: Long) {

        var askOrder: Order.Ask
        var bidOrder: Order.Bid

        while ((askOrder = getNextAskOrder(assetId)) != null && (bidOrder = getNextBidOrder(assetId)) != null) {

            if (askOrder.priceNQT > bidOrder.priceNQT) {
                break
            }


            val trade = tradeService.addTrade(assetId, dp.blockchain.lastBlock, askOrder, bidOrder)

            askOrderUpdateQuantityQNT(askOrder, Convert.safeSubtract(askOrder.quantityQNT, trade.quantityQNT))
            val askAccount = dp.accountService.getAccount(askOrder.accountId)
            dp.accountService.addToBalanceAndUnconfirmedBalanceNQT(askAccount, Convert.safeMultiply(trade.quantityQNT, trade.priceNQT))
            dp.accountService.addToAssetBalanceQNT(askAccount, assetId, -trade.quantityQNT)

            bidOrderUpdateQuantityQNT(bidOrder, Convert.safeSubtract(bidOrder.quantityQNT, trade.quantityQNT))
            val bidAccount = dp.accountService.getAccount(bidOrder.accountId)
            dp.accountService.addToAssetAndUnconfirmedAssetBalanceQNT(bidAccount, assetId, trade.quantityQNT)
            dp.accountService.addToBalanceNQT(bidAccount, -Convert.safeMultiply(trade.quantityQNT, trade.priceNQT))
            dp.accountService.addToUnconfirmedBalanceNQT(bidAccount, Convert.safeMultiply(trade.quantityQNT, bidOrder.priceNQT - trade.priceNQT))
        }
    }

    private fun askOrderUpdateQuantityQNT(askOrder: Ask, quantityQNT: Long) {
        askOrder.quantityQNT = quantityQNT
        if (quantityQNT > 0) {
            askOrderTable.insert(askOrder)
        } else if (quantityQNT == 0L) {
            askOrderTable.delete(askOrder)
        } else {
            throw IllegalArgumentException("Negative quantity: " + quantityQNT
                    + " for order: " + Convert.toUnsignedLong(askOrder.id))
        }
    }

    private fun bidOrderUpdateQuantityQNT(bidOrder: Bid, quantityQNT: Long) {
        bidOrder.quantityQNT = quantityQNT
        if (quantityQNT > 0) {
            bidOrderTable.insert(bidOrder)
        } else if (quantityQNT == 0L) {
            bidOrderTable.delete(bidOrder)
        } else {
            throw IllegalArgumentException("Negative quantity: " + quantityQNT
                    + " for order: " + Convert.toUnsignedLong(bidOrder.id))
        }
    }
}
