package brs.db.store

import brs.Trade
import brs.db.BurstKey
import brs.db.sql.EntitySqlTable

interface TradeStore {

    val tradeDbKeyFactory: BurstKey.LinkKeyFactory<Trade>

    val tradeTable: EntitySqlTable<Trade>
    suspend fun getAllTrades(from: Int, to: Int): Collection<Trade>

    suspend fun getAssetTrades(assetId: Long, from: Int, to: Int): Collection<Trade>

    suspend fun getAccountTrades(accountId: Long, from: Int, to: Int): Collection<Trade>

    suspend fun getAccountAssetTrades(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Trade>

    suspend fun getTradeCount(assetId: Long): Int
}
