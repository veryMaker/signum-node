package brs.services

import brs.entity.Account.AccountAsset
import brs.entity.Asset
import brs.entity.AssetTransfer
import brs.entity.Order.Ask
import brs.entity.Order.Bid
import brs.entity.Trade
import brs.entity.Trade.Event
import brs.entity.Transaction
import brs.transaction.appendix.Attachment.*

interface AssetExchangeService {
    /**
     * TODO
     */
    val assetsCount: Int

    /**
     * TODO
     */
    val askCount: Int

    /**
     * TODO
     */
    val bidCount: Int

    /**
     * TODO
     */
    val tradesCount: Int

    /**
     * TODO
     */
    val assetTransferCount: Int

    /**
     * TODO
     */
    fun getAllAssets(from: Int, to: Int): Collection<Asset>

    /**
     * TODO
     */
    fun getAsset(assetId: Long): Asset?

    /**
     * TODO
     */
    fun getTradeCount(assetId: Long): Int

    /**
     * TODO
     */
    fun getTransferCount(assetId: Long): Int

    /**
     * TODO
     */
    fun getAssetAccountsCount(assetId: Long): Int

    /**
     * TODO
     */
    fun addTradeListener(eventType: Event, listener: (Trade) -> Unit)

    /**
     * TODO
     */
    fun getAskOrder(orderId: Long): Ask?

    /**
     * TODO
     */
    fun addAsset(transaction: Transaction, attachment: ColoredCoinsAssetIssuance)

    /**
     * TODO
     */
    fun addAssetTransfer(transaction: Transaction, attachment: ColoredCoinsAssetTransfer)

    /**
     * TODO
     */
    fun addAskOrder(transaction: Transaction, attachment: ColoredCoinsAskOrderPlacement)

    /**
     * TODO
     */
    fun addBidOrder(transaction: Transaction, attachment: ColoredCoinsBidOrderPlacement)

    /**
     * TODO
     */
    fun removeAskOrder(orderId: Long)

    /**
     * TODO
     */
    fun getBidOrder(orderId: Long): Bid?

    /**
     * TODO
     */
    fun removeBidOrder(orderId: Long)

    /**
     * TODO
     */
    fun getAllTrades(from: Int, to: Int): Collection<Trade>

    /**
     * TODO
     */
    fun getTrades(assetId: Long, from: Int, to: Int): Collection<Trade>

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
    fun getAccountAssetsOverview(accountId: Long, height: Int, from: Int, to: Int): Collection<AccountAsset>

    /**
     * TODO
     */
    fun getAssetsIssuedBy(accountId: Long, from: Int, to: Int): Collection<Asset>

    /**
     * TODO
     */
    fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer>

    /**
     * TODO
     */
    fun getAccountAssetTransfers(accountId: Long, assetId: Long, from: Int, to: Int): Collection<AssetTransfer>

    /**
     * TODO
     */
    fun getAskOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Ask>

    /**
     * TODO
     */
    fun getAskOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Ask>

    /**
     * TODO
     */
    fun getBidOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Bid>

    /**
     * TODO
     */
    fun getBidOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Bid>

    /**
     * TODO
     */
    fun getAllAskOrders(from: Int, to: Int): Collection<Ask>

    /**
     * TODO
     */
    fun getAllBidOrders(from: Int, to: Int): Collection<Bid>

    /**
     * TODO
     */
    fun getSortedAskOrders(assetId: Long, from: Int, to: Int): Collection<Ask>

    /**
     * TODO
     */
    fun getSortedBidOrders(assetId: Long, from: Int, to: Int): Collection<Bid>
}
