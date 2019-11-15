package brs.db

import brs.entity.Trade

interface TradeStore {
    /**
     * TODO
     */
    val tradeDbKeyFactory: BurstKey.LinkKeyFactory<Trade>

    /**
     * TODO
     */
    val tradeTable: EntityTable<Trade>

    /**
     * TODO
     */
    fun getAllTrades(from: Int, to: Int): Collection<Trade>

    /**
     * TODO
     */
    fun getAssetTrades(assetId: Long, from: Int, to: Int): Collection<Trade>

    /**
     * TODO
     */
    fun getAccountTrades(accountId: Long, from: Int, to: Int): Collection<Trade>

    /**
     * TODO
     */
    fun getAccountAssetTrades(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Trade>

    /**
     * TODO
     */
    fun getTradeCount(assetId: Long): Int
}
