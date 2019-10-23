package brs.assetexchange

import brs.Account.AccountAsset
import brs.Asset
import brs.AssetTransfer
import brs.Attachment.*
import brs.Order.Ask
import brs.Order.Bid
import brs.Trade
import brs.Trade.Event
import brs.Transaction

interface AssetExchange {
    suspend fun getAssetsCount(): Int

    suspend fun getAskCount(): Int

    suspend fun getBidCount(): Int

    suspend fun getTradesCount(): Int

    suspend fun getAssetTransferCount(): Int

    suspend fun getAllAssets(from: Int, to: Int): Collection<Asset>

    suspend fun getAsset(assetId: Long): Asset?

    suspend fun getTradeCount(assetId: Long): Int

    suspend fun getTransferCount(assetId: Long): Int

    suspend fun getAssetAccountsCount(assetId: Long): Int

    suspend fun addTradeListener(eventType: Event, listener: suspend (Trade) -> Unit)

    suspend fun getAskOrder(orderId: Long): Ask?

    suspend fun addAsset(transaction: Transaction, attachment: ColoredCoinsAssetIssuance)

    suspend fun addAssetTransfer(transaction: Transaction, attachment: ColoredCoinsAssetTransfer)

    suspend fun addAskOrder(transaction: Transaction, attachment: ColoredCoinsAskOrderPlacement)

    suspend fun addBidOrder(transaction: Transaction, attachment: ColoredCoinsBidOrderPlacement)

    suspend fun removeAskOrder(orderId: Long)

    suspend fun getBidOrder(orderId: Long): Bid?

    suspend fun removeBidOrder(orderId: Long)

    suspend fun getAllTrades(i: Int, i1: Int): Collection<Trade>

    suspend fun getTrades(assetId: Long, from: Int, to: Int): Collection<Trade>

    suspend fun getAccountTrades(accountId: Long, from: Int, to: Int): Collection<Trade>

    suspend fun getAccountAssetTrades(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Trade>

    suspend fun getAccountAssetsOverview(accountId: Long, height: Int, from: Int, to: Int): Collection<AccountAsset>

    suspend fun getAssetsIssuedBy(accountId: Long, from: Int, to: Int): Collection<Asset>

    suspend fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer>

    suspend fun getAccountAssetTransfers(id: Long, id1: Long, from: Int, to: Int): Collection<AssetTransfer>

    suspend fun getAskOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Ask>

    suspend fun getAskOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Ask>

    suspend fun getBidOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Bid>

    suspend fun getBidOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Bid>

    suspend fun getAllAskOrders(from: Int, to: Int): Collection<Ask>

    suspend fun getAllBidOrders(from: Int, to: Int): Collection<Bid>

    suspend fun getSortedAskOrders(assetId: Long, from: Int, to: Int): Collection<Ask>

    suspend fun getSortedBidOrders(assetId: Long, from: Int, to: Int): Collection<Bid>
}
