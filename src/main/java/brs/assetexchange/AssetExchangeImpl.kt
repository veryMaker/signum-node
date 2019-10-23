package brs.assetexchange

import brs.*
import brs.Account.AccountAsset
import brs.Attachment.*
import brs.Order.Ask
import brs.Order.Bid
import brs.Trade.Event

class AssetExchangeImpl(dp: DependencyProvider) : AssetExchange {

    private val tradeService: TradeServiceImpl = TradeServiceImpl(dp.tradeStore)
    private val assetAccountService: AssetAccountServiceImpl = AssetAccountServiceImpl(dp.accountStore)
    private val assetTransferService: AssetTransferServiceImpl = AssetTransferServiceImpl(dp.assetTransferStore)
    private val assetService: AssetServiceImpl
    private val orderService: OrderServiceImpl

    override suspend fun getAssetsCount() = assetService.getAssetsCount()

    override suspend fun getAskCount() = orderService.getAskCount()

    override suspend fun getBidCount() = orderService.getBidCount()

    override suspend fun getTradesCount() = tradeService.getCount()

    override suspend fun getAssetTransferCount() = assetTransferService.getAssetTransferCount()


    init {
        this.assetService = AssetServiceImpl(this.assetAccountService, tradeService, dp.assetStore, assetTransferService)
        this.orderService = OrderServiceImpl(dp, tradeService)
    }

    override suspend fun getAllAssets(from: Int, to: Int): Collection<Asset> {
        return assetService.getAllAssets(from, to)
    }

    override suspend fun getAsset(assetId: Long): Asset? {
        return assetService.getAsset(assetId)
    }

    override suspend fun getTradeCount(assetId: Long): Int {
        return tradeService.getTradeCount(assetId)
    }

    override suspend fun getTransferCount(assetId: Long): Int {
        return assetTransferService.getTransferCount(assetId)
    }

    override suspend fun getAssetAccountsCount(assetId: Long): Int {
        return assetAccountService.getAssetAccountsCount(assetId)
    }

    override suspend fun addTradeListener(eventType: Event, listener: suspend (Trade) -> Unit) {
        tradeService.addListener(eventType, listener)
    }

    override suspend fun getAskOrder(orderId: Long): Ask? {
        return orderService.getAskOrder(orderId)
    }

    override suspend fun addAsset(transaction: Transaction, attachment: ColoredCoinsAssetIssuance) {
        assetService.addAsset(transaction, attachment)
    }

    override suspend fun addAssetTransfer(transaction: Transaction, attachment: ColoredCoinsAssetTransfer) {
        assetTransferService.addAssetTransfer(transaction, attachment)
    }

    override suspend fun addAskOrder(transaction: Transaction, attachment: ColoredCoinsAskOrderPlacement) {
        orderService.addAskOrder(transaction, attachment)
    }

    override suspend fun addBidOrder(transaction: Transaction, attachment: ColoredCoinsBidOrderPlacement) {
        orderService.addBidOrder(transaction, attachment)
    }

    override suspend fun removeAskOrder(orderId: Long) {
        orderService.removeAskOrder(orderId)
    }

    override suspend fun getBidOrder(orderId: Long): Bid? {
        return orderService.getBidOrder(orderId)
    }

    override suspend fun removeBidOrder(orderId: Long) {
        orderService.removeBidOrder(orderId)
    }

    override suspend fun getAllTrades(from: Int, to: Int): Collection<Trade> {
        return tradeService.getAllTrades(from, to)
    }

    override suspend fun getTrades(assetId: Long, from: Int, to: Int): Collection<Trade> {
        return assetService.getTrades(assetId, from, to)
    }

    override suspend fun getAccountTrades(id: Long, from: Int, to: Int): Collection<Trade> {
        return tradeService.getAccountTrades(id, from, to)
    }

    override suspend fun getAccountAssetTrades(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Trade> {
        return tradeService.getAccountAssetTrades(accountId, assetId, from, to)
    }

    override suspend fun getAccountAssetsOverview(id: Long, height: Int, from: Int, to: Int): Collection<AccountAsset> {
        return assetAccountService.getAssetAccounts(id, height, from, to)
    }

    override suspend fun getAssetsIssuedBy(accountId: Long, from: Int, to: Int): Collection<Asset> {
        return assetService.getAssetsIssuedBy(accountId, from, to)
    }

    override suspend fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer> {
        return assetTransferService.getAssetTransfers(assetId, from, to)
    }

    override suspend fun getAccountAssetTransfers(accountId: Long, assetId: Long, from: Int, to: Int): Collection<AssetTransfer> {
        return assetTransferService.getAccountAssetTransfers(accountId, assetId, from, to)
    }

    override suspend fun getAskOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Ask> {
        return orderService.getAskOrdersByAccount(accountId, from, to)
    }

    override suspend fun getAskOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Ask> {
        return orderService.getAskOrdersByAccountAsset(accountId, assetId, from, to)
    }

    override suspend fun getBidOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Bid> {
        return orderService.getBidOrdersByAccount(accountId, from, to)
    }

    override suspend fun getBidOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Bid> {
        return orderService.getBidOrdersByAccountAsset(accountId, assetId, from, to)
    }

    override suspend fun getAllAskOrders(from: Int, to: Int): Collection<Ask> {
        return orderService.getAllAskOrders(from, to)
    }

    override suspend fun getAllBidOrders(from: Int, to: Int): Collection<Bid> {
        return orderService.getAllBidOrders(from, to)
    }

    override suspend fun getSortedAskOrders(assetId: Long, from: Int, to: Int): Collection<Ask> {
        return orderService.getSortedAskOrders(assetId, from, to)
    }

    override suspend fun getSortedBidOrders(assetId: Long, from: Int, to: Int): Collection<Bid> {
        return orderService.getSortedBidOrders(assetId, from, to)
    }
}
