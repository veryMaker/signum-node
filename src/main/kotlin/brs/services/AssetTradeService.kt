package brs.services

import brs.db.BurstKey
import brs.db.EntityTable
import brs.entity.Block
import brs.entity.Order
import brs.entity.Trade
import brs.util.Listeners
import brs.util.Observable

interface AssetTradeService : Observable<Trade, Trade.Event> {
    /**
     * TODO
     */
    val listeners: Listeners<Trade, Trade.Event>

    /**
     * TODO
     */
    val tradeTable: EntityTable<Trade>

    /**
     * TODO
     */
    val tradeDbKeyFactory: BurstKey.LinkKeyFactory<Trade>

    /**
     * TODO
     */
    val count: Int

    /**
     * TODO
     */
    fun getAssetTrades(assetId: Long, from: Int, to: Int): Collection<Trade>

    /**
     * TODO
     */
    fun getAccountAssetTrades(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Trade>

    /**
     * TODO
     */
    fun getAccountTrades(id: Long, from: Int, to: Int): Collection<Trade>

    /**
     * TODO
     */
    fun getTradeCount(assetId: Long): Int

    /**
     * TODO
     */
    fun getAllTrades(from: Int, to: Int): Collection<Trade>

    /**
     * TODO
     */
    fun addTrade(assetId: Long, block: Block, askOrder: Order.Ask, bidOrder: Order.Bid): Trade
}
