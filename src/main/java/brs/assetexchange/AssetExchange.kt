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
    val assetsCount: Int

    val askCount: Int

    val bidCount: Int

    val tradesCount: Int

    val assetTransferCount: Int

    fun getAllAssets(from: Int, to: Int): Collection<Asset>

    fun getAsset(assetId: Long): Asset?

    fun getTradeCount(assetId: Long): Int

    fun getTransferCount(assetId: Long): Int

    fun getAssetAccountsCount(assetId: Long): Int

    fun addTradeListener(eventType: Event, listener: (Trade) -> Unit)

    fun getAskOrder(orderId: Long): Ask?

    fun addAsset(transaction: Transaction, attachment: ColoredCoinsAssetIssuance)

    fun addAssetTransfer(transaction: Transaction, attachment: ColoredCoinsAssetTransfer)

    fun addAskOrder(transaction: Transaction, attachment: ColoredCoinsAskOrderPlacement)

    fun addBidOrder(transaction: Transaction, attachment: ColoredCoinsBidOrderPlacement)

    fun removeAskOrder(orderId: Long)

    fun getBidOrder(orderId: Long): Bid?

    fun removeBidOrder(orderId: Long)

    fun getAllTrades(i: Int, i1: Int): Collection<Trade>

    fun getTrades(assetId: Long, from: Int, to: Int): Collection<Trade>

    fun getAccountTrades(accountId: Long, from: Int, to: Int): Collection<Trade>

    fun getAccountAssetTrades(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Trade>

    fun getAccountAssetsOverview(accountId: Long, height: Int, from: Int, to: Int): Collection<AccountAsset>

    fun getAssetsIssuedBy(accountId: Long, from: Int, to: Int): Collection<Asset>

    fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer>

    fun getAccountAssetTransfers(id: Long, id1: Long, from: Int, to: Int): Collection<AssetTransfer>

    fun getAskOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Ask>

    fun getAskOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Ask>

    fun getBidOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Bid>

    fun getBidOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Bid>

    fun getAllAskOrders(from: Int, to: Int): Collection<Ask>

    fun getAllBidOrders(from: Int, to: Int): Collection<Bid>

    fun getSortedAskOrders(assetId: Long, from: Int, to: Int): Collection<Ask>

    fun getSortedBidOrders(assetId: Long, from: Int, to: Int): Collection<Bid>
}
