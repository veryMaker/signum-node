package brs.services

import brs.entity.Order
import brs.db.BurstKey
import brs.db.VersionedEntityTable
import brs.entity.Transaction
import brs.transaction.appendix.Attachment

interface AssetOrderService {
    val askOrderTable: VersionedEntityTable<Order.Ask>
    val askOrderDbKeyFactory: BurstKey.LongKeyFactory<Order.Ask>
    val bidOrderTable: VersionedEntityTable<Order.Bid>
    val bidOrderDbKeyFactory: BurstKey.LongKeyFactory<Order.Bid>
    val bidCount: Int
    val askCount: Int
    fun getAskOrder(orderId: Long): Order.Ask?
    fun getBidOrder(orderId: Long): Order.Bid?
    fun getAllAskOrders(from: Int, to: Int): Collection<Order.Ask>
    fun getAllBidOrders(from: Int, to: Int): Collection<Order.Bid>
    fun getSortedBidOrders(assetId: Long, from: Int, to: Int): Collection<Order.Bid>
    fun getAskOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Order.Ask>
    fun getAskOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Order.Ask>
    fun getSortedAskOrders(assetId: Long, from: Int, to: Int): Collection<Order.Ask>
    fun getBidOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Order.Bid>
    fun getBidOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Order.Bid>
    fun removeBidOrder(orderId: Long)
    fun removeAskOrder(orderId: Long)
    fun addAskOrder(transaction: Transaction, attachment: Attachment.ColoredCoinsAskOrderPlacement)
    fun addBidOrder(transaction: Transaction, attachment: Attachment.ColoredCoinsBidOrderPlacement)
    fun getNextAskOrder(assetId: Long): Order.Ask?
    fun getNextBidOrder(assetId: Long): Order.Bid?
    fun matchOrders(assetId: Long)
    fun askOrderUpdateQuantity(askOrder: Order.Ask, quantity: Long)
    fun bidOrderUpdateQuantity(bidOrder: Order.Bid, quantity: Long)
}
