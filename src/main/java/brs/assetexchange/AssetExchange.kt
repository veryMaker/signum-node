package brs.assetexchange

import brs.Account.AccountAsset
import brs.*
import brs.Attachment.ColoredCoinsAskOrderPlacement
import brs.Attachment.ColoredCoinsAssetIssuance
import brs.Attachment.ColoredCoinsAssetTransfer
import brs.Attachment.ColoredCoinsBidOrderPlacement
import brs.Order.Ask
import brs.Order.Bid
import brs.Trade.Event
import java.util.function.Consumer

interface AssetExchange {

    val assetsCount: Int

    val askCount: Int

    val bidCount: Int

    val tradesCount: Int

    val assetTransferCount: Int

    fun getAllAssets(from: Int, to: Int): Collection<Asset>

    fun getAsset(assetId: Long): Asset

    fun getTradeCount(assetId: Long): Int

    fun getTransferCount(id: Long): Int

    fun getAssetAccountsCount(id: Long): Int

    fun addTradeListener(listener: Consumer<Trade>, trade: Event)

    fun getAskOrder(orderId: Long): Ask

    fun addAsset(transaction: Transaction, attachment: ColoredCoinsAssetIssuance)

    fun addAssetTransfer(transaction: Transaction, attachment: ColoredCoinsAssetTransfer)

    fun addAskOrder(transaction: Transaction, attachment: ColoredCoinsAskOrderPlacement)

    fun addBidOrder(transaction: Transaction, attachment: ColoredCoinsBidOrderPlacement)

    fun removeAskOrder(orderId: Long)

    fun getBidOrder(orderId: Long): Order.Bid

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
