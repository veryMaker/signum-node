package brs.services.impl

import brs.db.TradeStore
import brs.entity.Block
import brs.entity.Order
import brs.entity.Trade
import brs.entity.Trade.Event
import brs.services.AssetTradeService
import brs.util.Listeners

internal class AssetTradeServiceImpl(private val tradeStore: TradeStore) : AssetTradeService {

    override val listeners = Listeners<Trade, Event>()
    override val tradeTable = tradeStore.tradeTable
    override val tradeDbKeyFactory = tradeStore.tradeDbKeyFactory

    override val count get() = tradeTable.count

    override fun getAssetTrades(assetId: Long, from: Int, to: Int): Collection<Trade> {
        return tradeStore.getAssetTrades(assetId, from, to)
    }

    override fun getAccountAssetTrades(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Trade> {
        return tradeStore.getAccountAssetTrades(accountId, assetId, from, to)
    }

    override fun getAccountTrades(id: Long, from: Int, to: Int): Collection<Trade> {
        return tradeStore.getAccountTrades(id, from, to)
    }

    override fun getTradeCount(assetId: Long): Int {
        return tradeStore.getTradeCount(assetId)
    }

    override fun getAllTrades(from: Int, to: Int): Collection<Trade> {
        return tradeTable.getAll(from, to)
    }

    override fun addListener(eventType: Event, listener: (Trade) -> Unit) {
        listeners.addListener(eventType, listener)
    }

    override fun addTrade(assetId: Long, block: Block, askOrder: Order.Ask, bidOrder: Order.Bid): Trade {
        val dbKey = tradeDbKeyFactory.newKey(askOrder.id, bidOrder.id)
        val trade = Trade(dbKey, assetId, block, askOrder, bidOrder)
        tradeTable.insert(trade)
        listeners.accept(Event.TRADE, trade)
        return trade
    }
}
