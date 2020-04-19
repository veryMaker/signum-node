package brs.services

import brs.db.BurstKey
import brs.db.MutableEntityTable
import brs.entity.Order
import brs.entity.Transaction
import brs.transaction.appendix.Attachment

interface AssetOrderService {
    /**
     * TODO
     */
    val askOrderTable: MutableEntityTable<Order.Ask>

    /**
     * TODO
     */
    val askOrderDbKeyFactory: BurstKey.LongKeyFactory<Order.Ask>

    /**
     * TODO
     */
    val bidOrderTable: MutableEntityTable<Order.Bid>

    /**
     * TODO
     */
    val bidOrderDbKeyFactory: BurstKey.LongKeyFactory<Order.Bid>

    /**
     * TODO
     */
    val bidCount: Int

    /**
     * TODO
     */
    val askCount: Int

    /**
     * TODO
     */
    fun getAskOrder(orderId: Long): Order.Ask?

    /**
     * TODO
     */
    fun getBidOrder(orderId: Long): Order.Bid?

    /**
     * TODO
     */
    fun getAllAskOrders(from: Int, to: Int): Collection<Order.Ask>

    /**
     * TODO
     */
    fun getAllBidOrders(from: Int, to: Int): Collection<Order.Bid>

    /**
     * TODO
     */
    fun getSortedBidOrders(assetId: Long, from: Int, to: Int): Collection<Order.Bid>

    /**
     * TODO
     */
    fun getAskOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Order.Ask>

    /**
     * TODO
     */
    fun getAskOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Order.Ask>

    /**
     * TODO
     */
    fun getSortedAskOrders(assetId: Long, from: Int, to: Int): Collection<Order.Ask>

    /**
     * TODO
     */
    fun getBidOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Order.Bid>

    /**
     * TODO
     */
    fun getBidOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Order.Bid>

    /**
     * TODO
     */
    fun removeBidOrder(orderId: Long)

    /**
     * TODO
     */
    fun removeAskOrder(orderId: Long)

    /**
     * TODO
     */
    fun addAskOrder(transaction: Transaction, attachment: Attachment.ColoredCoinsAskOrderPlacement)

    /**
     * TODO
     */
    fun addBidOrder(transaction: Transaction, attachment: Attachment.ColoredCoinsBidOrderPlacement)

    /**
     * TODO
     */
    fun getNextAskOrder(assetId: Long): Order.Ask?

    /**
     * TODO
     */
    fun getNextBidOrder(assetId: Long): Order.Bid?

    /**
     * TODO
     */
    fun matchOrders(assetId: Long)

    /**
     * TODO
     */
    fun askOrderUpdateQuantity(askOrder: Order.Ask, quantity: Long)

    /**
     * TODO
     */
    fun bidOrderUpdateQuantity(bidOrder: Order.Bid, quantity: Long)
}
