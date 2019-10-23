package brs.db.store

import brs.Order
import brs.db.BurstKey
import brs.db.VersionedEntityTable

interface OrderStore {
    val bidOrderTable: VersionedEntityTable<Order.Bid>

    val askOrderDbKeyFactory: BurstKey.LongKeyFactory<Order.Ask>

    val askOrderTable: VersionedEntityTable<Order.Ask>

    val bidOrderDbKeyFactory: BurstKey.LongKeyFactory<Order.Bid>

    suspend fun getAskOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Order.Ask>

    suspend fun getSortedAsks(assetId: Long, from: Int, to: Int): Collection<Order.Ask>

    suspend fun getNextOrder(assetId: Long): Order.Ask?

    suspend fun getAll(from: Int, to: Int): Collection<Order.Ask>

    suspend fun getAskOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Order.Ask>

    suspend fun getAskOrdersByAsset(assetId: Long, from: Int, to: Int): Collection<Order.Ask>

    suspend fun getBidOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Order.Bid>

    suspend fun getBidOrdersByAsset(assetId: Long, from: Int, to: Int): Collection<Order.Bid>

    suspend fun getBidOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Order.Bid>

    suspend fun getSortedBids(assetId: Long, from: Int, to: Int): Collection<Order.Bid>

    suspend fun getNextBid(assetId: Long): Order.Bid?
}
