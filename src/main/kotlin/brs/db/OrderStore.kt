package brs.db

import brs.entity.Order

interface OrderStore {
    /**
     * TODO
     */
    val bidOrderTable: MutableEntityTable<Order.Bid>

    /**
     * TODO
     */
    val askOrderDbKeyFactory: BurstKey.LongKeyFactory<Order.Ask>

    /**
     * TODO
     */
    val askOrderTable: MutableEntityTable<Order.Ask>

    /**
     * TODO
     */
    val bidOrderDbKeyFactory: BurstKey.LongKeyFactory<Order.Bid>

    /**
     * TODO
     */
    fun getAskOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Order.Ask>

    /**
     * TODO
     */
    fun getSortedAsks(assetId: Long, from: Int, to: Int): Collection<Order.Ask>

    /**
     * TODO
     */
    fun getNextOrder(assetId: Long): Order.Ask?

    /**
     * TODO
     */
    fun getAll(from: Int, to: Int): Collection<Order.Ask>

    /**
     * TODO
     */
    fun getAskOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Order.Ask>

    /**
     * TODO
     */
    fun getAskOrdersByAsset(assetId: Long, from: Int, to: Int): Collection<Order.Ask>

    /**
     * TODO
     */
    fun getBidOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Order.Bid>

    /**
     * TODO
     */
    fun getBidOrdersByAsset(assetId: Long, from: Int, to: Int): Collection<Order.Bid>

    /**
     * TODO
     */
    fun getBidOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Order.Bid>

    /**
     * TODO
     */
    fun getSortedBids(assetId: Long, from: Int, to: Int): Collection<Order.Bid>

    /**
     * TODO
     */
    fun getNextBid(assetId: Long): Order.Bid?
}
