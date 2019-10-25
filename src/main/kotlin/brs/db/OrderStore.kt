package brs.db

import brs.entity.Order
import brs.db.BurstKey
import brs.db.VersionedEntityTable

interface OrderStore {
    val bidOrderTable: VersionedEntityTable<Order.Bid>

    val askOrderDbKeyFactory: BurstKey.LongKeyFactory<Order.Ask>

    val askOrderTable: VersionedEntityTable<Order.Ask>

    val bidOrderDbKeyFactory: BurstKey.LongKeyFactory<Order.Bid>

    fun getAskOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Order.Ask>

    fun getSortedAsks(assetId: Long, from: Int, to: Int): Collection<Order.Ask>

    fun getNextOrder(assetId: Long): Order.Ask?

    fun getAll(from: Int, to: Int): Collection<Order.Ask>

    fun getAskOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Order.Ask>

    fun getAskOrdersByAsset(assetId: Long, from: Int, to: Int): Collection<Order.Ask>

    fun getBidOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Order.Bid>

    fun getBidOrdersByAsset(assetId: Long, from: Int, to: Int): Collection<Order.Bid>

    fun getBidOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Order.Bid>

    fun getSortedBids(assetId: Long, from: Int, to: Int): Collection<Order.Bid>

    fun getNextBid(assetId: Long): Order.Bid?
}
