package brs.db.store

import brs.entity.Trade
import brs.db.BurstKey
import brs.db.sql.EntitySqlTable

interface TradeStore {

    val tradeDbKeyFactory: BurstKey.LinkKeyFactory<Trade>

    val tradeTable: EntitySqlTable<Trade>
    fun getAllTrades(from: Int, to: Int): Collection<Trade>

    fun getAssetTrades(assetId: Long, from: Int, to: Int): Collection<Trade>

    fun getAccountTrades(accountId: Long, from: Int, to: Int): Collection<Trade>

    fun getAccountAssetTrades(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Trade>

    fun getTradeCount(assetId: Long): Int
}
