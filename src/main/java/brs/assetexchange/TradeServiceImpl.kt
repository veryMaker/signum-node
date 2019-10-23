package brs.assetexchange

import brs.Block
import brs.Order
import brs.Trade
import brs.Trade.Event
import brs.db.store.TradeStore
import brs.util.Listeners
import brs.util.Observable

internal class TradeServiceImpl(private val tradeStore: TradeStore): Observable<Trade, Event> { // TODO add interface

    private val listeners = Listeners<Trade, Event>()
    private val tradeTable = tradeStore.tradeTable
    private val tradeDbKeyFactory = tradeStore.tradeDbKeyFactory

    suspend fun getCount() = tradeTable.getCount()

    suspend fun getAssetTrades(assetId: Long, from: Int, to: Int): Collection<Trade> {
        return tradeStore.getAssetTrades(assetId, from, to)
    }

    suspend fun getAccountAssetTrades(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Trade> {
        return tradeStore.getAccountAssetTrades(accountId, assetId, from, to)
    }

    suspend fun getAccountTrades(id: Long, from: Int, to: Int): Collection<Trade> {
        return tradeStore.getAccountTrades(id, from, to)
    }

    suspend fun getTradeCount(assetId: Long): Int {
        return tradeStore.getTradeCount(assetId)
    }

    suspend fun getAllTrades(from: Int, to: Int): Collection<Trade> {
        return tradeTable.getAll(from, to)
    }

    override suspend fun addListener(eventType: Event, listener: suspend (Trade) -> Unit) {
        listeners.addListener(eventType, listener)
    }

    suspend fun addTrade(assetId: Long, block: Block, askOrder: Order.Ask, bidOrder: Order.Bid): Trade {
        val dbKey = tradeDbKeyFactory.newKey(askOrder.id, bidOrder.id)
        val trade = Trade(dbKey, assetId, block, askOrder, bidOrder)
        tradeTable.insert(trade)
        listeners.accept(Event.TRADE, trade)
        return trade
    }
}
