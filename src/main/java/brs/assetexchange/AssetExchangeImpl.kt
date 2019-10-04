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
    private val assetTransferService: AssetTransferServiceImpl
    private val assetService: AssetServiceImpl
    private val orderService: OrderServiceImpl

    override val assetsCount: Int
        get() = assetService.assetsCount

    override val askCount: Int
        get() = orderService.askCount

    override val bidCount: Int
        get() = orderService.bidCount

    override val tradesCount: Int
        get() = tradeService.count

    override val assetTransferCount: Int
        get() = assetTransferService.assetTransferCount


    init {
        this.assetTransferService = AssetTransferServiceImpl(dp.assetTransferStore)
        this.assetService = AssetServiceImpl(this.assetAccountService, tradeService, dp.assetStore, assetTransferService)
        this.orderService = OrderServiceImpl(dp, tradeService)
    }

    override fun getAllAssets(from: Int, to: Int): Collection<Asset> {
        return assetService.getAllAssets(from, to)
    }

    override fun getAsset(assetId: Long): Asset? {
        return assetService.getAsset(assetId)
    }

    override fun getTradeCount(assetId: Long): Int {
        return tradeService.getTradeCount(assetId)
    }

    override fun getTransferCount(assetId: Long): Int {
        return assetTransferService.getTransferCount(assetId)
    }

    override fun getAssetAccountsCount(assetId: Long): Int {
        return assetAccountService.getAssetAccountsCount(assetId)
    }

    override suspend fun addTradeListener(eventType: Event, listener: suspend (Trade) -> Unit) {
        tradeService.addListener(eventType, listener)
    }

    override fun getAskOrder(orderId: Long): Ask? {
        return orderService.getAskOrder(orderId)
    }

    override fun addAsset(transaction: Transaction, attachment: ColoredCoinsAssetIssuance) {
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

    override fun removeAskOrder(orderId: Long) {
        orderService.removeAskOrder(orderId)
    }

    override fun getBidOrder(orderId: Long): Bid? {
        return orderService.getBidOrder(orderId)
    }

    override fun removeBidOrder(orderId: Long) {
        orderService.removeBidOrder(orderId)
    }

    override fun getAllTrades(from: Int, to: Int): Collection<Trade> {
        return tradeService.getAllTrades(from, to)
    }

    override fun getTrades(assetId: Long, from: Int, to: Int): Collection<Trade> {
        return assetService.getTrades(assetId, from, to)
    }

    override fun getAccountTrades(id: Long, from: Int, to: Int): Collection<Trade> {
        return tradeService.getAccountTrades(id, from, to)
    }

    override fun getAccountAssetTrades(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Trade> {
        return tradeService.getAccountAssetTrades(accountId, assetId, from, to)
    }

    override fun getAccountAssetsOverview(id: Long, height: Int, from: Int, to: Int): Collection<AccountAsset> {
        return assetAccountService.getAssetAccounts(id, height, from, to)
    }

    override fun getAssetsIssuedBy(accountId: Long, from: Int, to: Int): Collection<Asset> {
        return assetService.getAssetsIssuedBy(accountId, from, to)
    }

    override fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer> {
        return assetTransferService.getAssetTransfers(assetId, from, to)
    }

    override fun getAccountAssetTransfers(accountId: Long, assetId: Long, from: Int, to: Int): Collection<AssetTransfer> {
        return assetTransferService.getAccountAssetTransfers(accountId, assetId, from, to)
    }

    override fun getAskOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Ask> {
        return orderService.getAskOrdersByAccount(accountId, from, to)
    }

    override fun getAskOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Ask> {
        return orderService.getAskOrdersByAccountAsset(accountId, assetId, from, to)
    }

    override fun getBidOrdersByAccount(accountId: Long, from: Int, to: Int): Collection<Bid> {
        return orderService.getBidOrdersByAccount(accountId, from, to)
    }

    override fun getBidOrdersByAccountAsset(accountId: Long, assetId: Long, from: Int, to: Int): Collection<Bid> {
        return orderService.getBidOrdersByAccountAsset(accountId, assetId, from, to)
    }

    override fun getAllAskOrders(from: Int, to: Int): Collection<Ask> {
        return orderService.getAllAskOrders(from, to)
    }

    override fun getAllBidOrders(from: Int, to: Int): Collection<Bid> {
        return orderService.getAllBidOrders(from, to)
    }

    override fun getSortedAskOrders(assetId: Long, from: Int, to: Int): Collection<Ask> {
        return orderService.getSortedAskOrders(assetId, from, to)
    }

    override fun getSortedBidOrders(assetId: Long, from: Int, to: Int): Collection<Bid> {
        return orderService.getSortedBidOrders(assetId, from, to)
    }
}
