package brs.services

import brs.db.BurstKey
import brs.db.EntityTable
import brs.entity.Block
import brs.entity.Order
import brs.entity.Trade
import brs.util.Listeners
import brs.util.Observable

interface AssetTradeService : Observable<Trade, Trade.Event> {
    val listeners: Listeners<Trade, Trade.Event>
    val tradeTable: EntityTable<Trade>
    val tradeDbKeyFactory: BurstKey.LinkKeyFactory<Trade>
    val count: Int
    fun getAssetTrades(assetId: Long, from: Int, to: Int): Collection<Trade>
    fun getAccountAssetTrades(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Trade>
    fun getAccountTrades(id: Long, from: Int, to: Int): Collection<Trade>
    fun getTradeCount(assetId: Long): Int
    fun getAllTrades(from: Int, to: Int): Collection<Trade>
    override fun addListener(eventType: Trade.Event, listener: (Trade) -> Unit)
    fun addTrade(assetId: Long, block: Block, askOrder: Order.Ask, bidOrder: Order.Bid): Trade
}
