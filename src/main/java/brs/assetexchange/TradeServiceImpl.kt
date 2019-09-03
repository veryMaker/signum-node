package brs.assetexchange

import brs.Block
import brs.Order
import brs.Trade
import brs.Trade.Event
import brs.db.BurstKey
import brs.db.BurstKey.LinkKeyFactory
import brs.db.sql.EntitySqlTable
import brs.db.store.TradeStore
import brs.util.Listeners
import java.util.function.Consumer

internal class TradeServiceImpl(private val tradeStore: TradeStore) { // TODO add interface

    private val listeners = Listeners<Trade, Event>()
    private val tradeTable = tradeStore.tradeTable
    private val tradeDbKeyFactory = tradeStore.tradeDbKeyFactory

    val count: Int
        get() = tradeTable.count


    fun getAssetTrades(assetId: Long, from: Int, to: Int): Collection<Trade> {
        return tradeStore.getAssetTrades(assetId, from, to)
    }

    fun getAccountAssetTrades(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Trade> {
        return tradeStore.getAccountAssetTrades(accountId, assetId, from, to)
    }

    fun getAccountTrades(id: Long, from: Int, to: Int): Collection<Trade> {
        return tradeStore.getAccountTrades(id, from, to)
    }

    fun getTradeCount(assetId: Long): Int {
        return tradeStore.getTradeCount(assetId)
    }

    fun getAllTrades(from: Int, to: Int): Collection<Trade> {
        return tradeTable.getAll(from, to)
    }

    fun addListener(listener: (Trade) -> Unit, eventType: Event): Boolean {
        return listeners.addListener(listener, eventType)
    }

    fun removeListener(listener: (Trade) -> Unit, eventType: Event): Boolean {
        return listeners.removeListener(listener, eventType)
    }

    fun addTrade(assetId: Long, block: Block, askOrder: Order.Ask, bidOrder: Order.Bid): Trade {
        val dbKey = tradeDbKeyFactory.newKey(askOrder.id, bidOrder.id)
        val trade = Trade(dbKey, assetId, block, askOrder, bidOrder)
        tradeTable.insert(trade)
        listeners.accept(trade, Event.TRADE)
        return trade
    }
}
