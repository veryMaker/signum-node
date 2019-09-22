package brs.grpc.proto

import brs.DependencyProvider
import brs.grpc.GrpcApiHandler
import brs.grpc.handlers.*
import com.google.protobuf.Empty
import com.google.protobuf.Message
import io.grpc.stub.StreamObserver
import kotlin.reflect.KClass

class BrsService(dp: DependencyProvider) : BrsApiServiceGrpc.BrsApiServiceImplBase() {

    private val handlers: Map<KClass<out GrpcApiHandler<out Message, out Message>>, GrpcApiHandler<out Message, out Message>>

    init { // TODO each handler should only take the dp
        val handlerMap = mutableMapOf<KClass<out GrpcApiHandler<out Message, out Message>>, GrpcApiHandler<out Message, out Message>>()
        handlerMap[BroadcastTransactionHandler::class] = BroadcastTransactionHandler(dp)
        handlerMap[BroadcastTransactionBytesHandler::class] = BroadcastTransactionBytesHandler(dp)
        handlerMap[CompleteBasicTransactionHandler::class] = CompleteBasicTransactionHandler(dp.timeService, dp.transactionProcessor, dp.blockchain)
        handlerMap[GetAccountATsHandler::class] = GetAccountATsHandler(dp.atService, dp.accountService)
        handlerMap[GetAccountBlocksHandler::class] = GetAccountBlocksHandler(dp.blockchain, dp.blockService, dp.accountService)
        handlerMap[GetAccountCurrentOrdersHandler::class] = GetAccountCurrentOrdersHandler(dp.assetExchange)
        handlerMap[GetAccountEscrowTransactionsHandler::class] = GetAccountEscrowTransactionsHandler(dp.escrowService)
        handlerMap[GetAccountHandler::class] = GetAccountHandler(dp.accountService)
        handlerMap[GetAccountsHandler::class] = GetAccountsHandler(dp.accountService)
        handlerMap[GetAccountSubscriptionsHandler::class] = GetAccountSubscriptionsHandler(dp.subscriptionService)
        handlerMap[GetAccountTransactionsHandler::class] = GetAccountTransactionsHandler(dp.blockchain, dp.accountService)
        handlerMap[GetAliasesHandler::class] = GetAliasesHandler(dp.aliasService)
        handlerMap[GetAliasHandler::class] = GetAliasHandler(dp.aliasService)
        handlerMap[GetAssetBalancesHandler::class] = GetAssetBalancesHandler(dp.assetExchange)
        handlerMap[GetAssetHandler::class] = GetAssetHandler(dp.assetExchange)
        handlerMap[GetAssetsByIssuerHandler::class] = GetAssetsByIssuerHandler(dp.assetExchange)
        handlerMap[GetAssetsHandler::class] = GetAssetsHandler(dp.assetExchange)
        handlerMap[GetAssetTradesHandler::class] = GetAssetTradesHandler(dp.assetExchange)
        handlerMap[GetAssetTransfersHandler::class] = GetAssetTransfersHandler(dp.assetExchange, dp.accountService)
        handlerMap[GetATHandler::class] = GetATHandler(dp.atService, dp.accountService)
        handlerMap[GetATIdsHandler::class] = GetATIdsHandler(dp.atService)
        handlerMap[GetBlockHandler::class] = GetBlockHandler(dp.blockchain, dp.blockService)
        handlerMap[GetBlocksHandler::class] = GetBlocksHandler(dp.blockchain, dp.blockService)
        handlerMap[GetConstantsHandler::class] = GetConstantsHandler(dp.fluxCapacitor)
        handlerMap[GetCountsHandler::class] = GetCountsHandler(dp)
        handlerMap[GetCurrentTimeHandler::class] = GetCurrentTimeHandler(dp.timeService)
        handlerMap[GetDgsGoodHandler::class] = GetDgsGoodHandler(dp.digitalGoodsStoreService)
        handlerMap[GetDgsGoodsHandler::class] = GetDgsGoodsHandler(dp.digitalGoodsStoreService)
        handlerMap[GetDgsPendingPurchasesHandler::class] = GetDgsPendingPurchasesHandler(dp.digitalGoodsStoreService)
        handlerMap[GetDgsPurchaseHandler::class] = GetDgsPurchaseHandler(dp.digitalGoodsStoreService)
        handlerMap[GetDgsPurchasesHandler::class] = GetDgsPurchasesHandler(dp.digitalGoodsStoreService)
        handlerMap[GetEscrowTransactionHandler::class] = GetEscrowTransactionHandler(dp.escrowService)
        handlerMap[GetMiningInfoHandler::class] = GetMiningInfoHandler(dp.blockchainProcessor, dp.blockchain, dp.generator)
        handlerMap[GetOrderHandler::class] = GetOrderHandler(dp.assetExchange)
        handlerMap[GetOrdersHandler::class] = GetOrdersHandler(dp.assetExchange)
        handlerMap[GetPeerHandler::class] = GetPeerHandler(dp)
        handlerMap[GetPeersHandler::class] = GetPeersHandler(dp)
        handlerMap[GetStateHandler::class] = GetStateHandler(dp)
        handlerMap[GetSubscriptionHandler::class] = GetSubscriptionHandler(dp.subscriptionService)
        handlerMap[GetSubscriptionsToAccountHandler::class] = GetSubscriptionsToAccountHandler(dp.subscriptionService)
        handlerMap[GetTransactionBytesHandler::class] = GetTransactionBytesHandler(dp)
        handlerMap[GetTransactionHandler::class] = GetTransactionHandler(dp.blockchain, dp.transactionProcessor)
        handlerMap[GetUnconfirmedTransactionsHandler::class] = GetUnconfirmedTransactionsHandler(dp.indirectIncomingService, dp.transactionProcessor)
        handlerMap[ParseTransactionHandler::class] = ParseTransactionHandler(dp)
        handlerMap[SubmitNonceHandler::class] = SubmitNonceHandler(dp.propertyService, dp.blockchain, dp.accountService, dp.generator)
        handlerMap[SuggestFeeHandler::class] = SuggestFeeHandler(dp.feeSuggestionCalculator)
        this.handlers = handlerMap
    }

    // TODO can we remove handlerClass? Will it still work?
    private inline fun <reified H : GrpcApiHandler<R, S>, R : Message, S : Message> handleRequest(handlerClass: KClass<H>, request: R, response: StreamObserver<S>) {
        val handler = handlers[H::class]
        if (handler is H) {
            handler.handleRequest(request, response)
        } else {
            response.onError(ProtoBuilder.buildError(HandlerNotFoundException("H not registered: ${H::class}")))
        }
    }

    override fun getMiningInfo(request: Empty, responseObserver: StreamObserver<BrsApi.MiningInfo>) {
        handleRequest(GetMiningInfoHandler::class, request, responseObserver)
    }

    override fun submitNonce(request: BrsApi.SubmitNonceRequest, responseObserver: StreamObserver<BrsApi.SubmitNonceResponse>) {
        handleRequest(SubmitNonceHandler::class, request, responseObserver)
    }

    override fun getAccount(request: BrsApi.GetAccountRequest, responseObserver: StreamObserver<BrsApi.Account>) {
        handleRequest(GetAccountHandler::class, request, responseObserver)
    }

    override fun getAccounts(request: BrsApi.GetAccountsRequest, responseObserver: StreamObserver<BrsApi.Accounts>) {
        handleRequest(GetAccountsHandler::class, request, responseObserver)
    }

    override fun getBlock(request: BrsApi.GetBlockRequest, responseObserver: StreamObserver<BrsApi.Block>) {
        handleRequest(GetBlockHandler::class, request, responseObserver)
    }

    override fun getTransaction(request: BrsApi.GetTransactionRequest, responseObserver: StreamObserver<BrsApi.Transaction>) {
        handleRequest(GetTransactionHandler::class, request, responseObserver)
    }

    override fun getTransactionBytes(request: BrsApi.BasicTransaction, responseObserver: StreamObserver<BrsApi.TransactionBytes>) {
        handleRequest(GetTransactionBytesHandler::class, request, responseObserver)
    }

    override fun completeBasicTransaction(request: BrsApi.BasicTransaction, responseObserver: StreamObserver<BrsApi.BasicTransaction>) {
        handleRequest(CompleteBasicTransactionHandler::class, request, responseObserver)
    }

    override fun getCurrentTime(request: Empty, responseObserver: StreamObserver<BrsApi.Time>) {
        handleRequest(GetCurrentTimeHandler::class, request, responseObserver)
    }

    override fun broadcastTransaction(request: BrsApi.BasicTransaction, responseObserver: StreamObserver<BrsApi.TransactionBroadcastResult>) {
        handleRequest(BroadcastTransactionHandler::class, request, responseObserver)
    }

    override fun broadcastTransactionBytes(request: BrsApi.TransactionBytes, responseObserver: StreamObserver<BrsApi.TransactionBroadcastResult>) {
        handleRequest(BroadcastTransactionBytesHandler::class, request, responseObserver)
    }

    override fun getState(request: Empty, responseObserver: StreamObserver<BrsApi.State>) {
        handleRequest(GetStateHandler::class, request, responseObserver)
    }

    override fun getPeers(request: BrsApi.GetPeersRequest, responseObserver: StreamObserver<BrsApi.Peers>) {
        handleRequest(GetPeersHandler::class, request, responseObserver)
    }

    override fun getPeer(request: BrsApi.GetPeerRequest, responseObserver: StreamObserver<BrsApi.Peer>) {
        handleRequest(GetPeerHandler::class, request, responseObserver)
    }

    override fun suggestFee(request: Empty, responseObserver: StreamObserver<BrsApi.FeeSuggestion>) {
        handleRequest(SuggestFeeHandler::class, request, responseObserver)
    }

    override fun parseTransaction(request: BrsApi.TransactionBytes, responseObserver: StreamObserver<BrsApi.BasicTransaction>) {
        handleRequest(ParseTransactionHandler::class, request, responseObserver)
    }

    override fun getAccountATs(request: BrsApi.GetAccountRequest, responseObserver: StreamObserver<BrsApi.AccountATs>) {
        handleRequest(GetAccountATsHandler::class, request, responseObserver)
    }

    override fun getAT(request: BrsApi.GetByIdRequest, responseObserver: StreamObserver<BrsApi.AT>) {
        handleRequest(GetATHandler::class, request, responseObserver)
    }

    override fun getATIds(request: Empty, responseObserver: StreamObserver<BrsApi.ATIds>) {
        handleRequest(GetATIdsHandler::class, request, responseObserver)
    }

    override fun getAlias(request: BrsApi.GetAliasRequest, responseObserver: StreamObserver<BrsApi.Alias>) {
        handleRequest(GetAliasHandler::class, request, responseObserver)
    }

    override fun getAliases(request: BrsApi.GetAliasesRequest, responseObserver: StreamObserver<BrsApi.Aliases>) {
        handleRequest(GetAliasesHandler::class, request, responseObserver)
    }

    override fun getUnconfirmedTransactions(request: BrsApi.GetAccountRequest, responseObserver: StreamObserver<BrsApi.UnconfirmedTransactions>) {
        handleRequest(GetUnconfirmedTransactionsHandler::class, request, responseObserver)
    }

    override fun getAccountBlocks(request: BrsApi.GetAccountBlocksRequest, responseObserver: StreamObserver<BrsApi.Blocks>) {
        handleRequest(GetAccountBlocksHandler::class, request, responseObserver)
    }

    override fun getAccountCurrentOrders(request: BrsApi.GetAccountOrdersRequest, responseObserver: StreamObserver<BrsApi.Orders>) {
        handleRequest(GetAccountCurrentOrdersHandler::class, request, responseObserver)
    }

    override fun getAccountEscrowTransactions(request: BrsApi.GetAccountRequest, responseObserver: StreamObserver<BrsApi.EscrowTransactions>) {
        handleRequest(GetAccountEscrowTransactionsHandler::class, request, responseObserver)
    }

    override fun getAccountSubscriptions(request: BrsApi.GetAccountRequest, responseObserver: StreamObserver<BrsApi.Subscriptions>) {
        handleRequest(GetAccountSubscriptionsHandler::class, request, responseObserver)
    }

    override fun getAccountTransactions(request: BrsApi.GetAccountTransactionsRequest, responseObserver: StreamObserver<BrsApi.Transactions>) {
        handleRequest(GetAccountTransactionsHandler::class, request, responseObserver)
    }

    override fun getAsset(request: BrsApi.GetByIdRequest, responseObserver: StreamObserver<BrsApi.Asset>) {
        handleRequest(GetAssetHandler::class, request, responseObserver)
    }

    override fun getAssetBalances(request: BrsApi.GetAssetBalancesRequest, responseObserver: StreamObserver<BrsApi.AssetBalances>) {
        handleRequest(GetAssetBalancesHandler::class, request, responseObserver)
    }

    override fun getAssets(request: BrsApi.GetAssetsRequest, responseObserver: StreamObserver<BrsApi.Assets>) {
        handleRequest(GetAssetsHandler::class, request, responseObserver)
    }

    override fun getAssetsByIssuer(request: BrsApi.GetAccountRequest, responseObserver: StreamObserver<BrsApi.Assets>) {
        handleRequest(GetAssetsByIssuerHandler::class, request, responseObserver)
    }

    override fun getAssetTrades(request: BrsApi.GetAssetTransfersRequest, responseObserver: StreamObserver<BrsApi.AssetTrades>) {
        handleRequest(GetAssetTradesHandler::class, request, responseObserver)
    }

    override fun getAssetTransfers(request: BrsApi.GetAssetTransfersRequest, responseObserver: StreamObserver<BrsApi.AssetTransfers>) {
        handleRequest(GetAssetTransfersHandler::class, request, responseObserver)
    }

    override fun getBlocks(request: BrsApi.GetBlocksRequest, responseObserver: StreamObserver<BrsApi.Blocks>) {
        handleRequest(GetBlocksHandler::class, request, responseObserver)
    }

    override fun getConstants(request: Empty, responseObserver: StreamObserver<BrsApi.Constants>) {
        handleRequest(GetConstantsHandler::class, request, responseObserver)
    }

    override fun getCounts(request: Empty, responseObserver: StreamObserver<BrsApi.Counts>) {
        handleRequest(GetCountsHandler::class, request, responseObserver)
    }

    override fun getDgsGood(request: BrsApi.GetByIdRequest, responseObserver: StreamObserver<BrsApi.DgsGood>) {
        handleRequest(GetDgsGoodHandler::class, request, responseObserver)
    }

    override fun getDgsGoods(request: BrsApi.GetDgsGoodsRequest, responseObserver: StreamObserver<BrsApi.DgsGoods>) {
        handleRequest(GetDgsGoodsHandler::class, request, responseObserver)
    }

    override fun getDgsPendingPurchases(request: BrsApi.GetDgsPendingPurchasesRequest, responseObserver: StreamObserver<BrsApi.DgsPurchases>) {
        handleRequest(GetDgsPendingPurchasesHandler::class, request, responseObserver)
    }

    override fun getDgsPurchase(request: BrsApi.GetByIdRequest, responseObserver: StreamObserver<BrsApi.DgsPurchase>) {
        handleRequest(GetDgsPurchaseHandler::class, request, responseObserver)
    }

    override fun getDgsPurchases(request: BrsApi.GetDgsPurchasesRequest, responseObserver: StreamObserver<BrsApi.DgsPurchases>) {
        handleRequest(GetDgsPurchasesHandler::class, request, responseObserver)
    }

    override fun getEscrowTransaction(request: BrsApi.GetByIdRequest, responseObserver: StreamObserver<BrsApi.EscrowTransaction>) {
        handleRequest(GetEscrowTransactionHandler::class, request, responseObserver)
    }

    override fun getOrder(request: BrsApi.GetOrderRequest, responseObserver: StreamObserver<BrsApi.Order>) {
        handleRequest(GetOrderHandler::class, request, responseObserver)
    }

    override fun getOrders(request: BrsApi.GetOrdersRequest, responseObserver: StreamObserver<BrsApi.Orders>) {
        handleRequest(GetOrdersHandler::class, request, responseObserver)
    }

    override fun getSubscription(request: BrsApi.GetByIdRequest, responseObserver: StreamObserver<BrsApi.Subscription>) {
        handleRequest(GetSubscriptionHandler::class, request, responseObserver)
    }

    override fun getSubscriptionsToAccount(request: BrsApi.GetAccountRequest, responseObserver: StreamObserver<BrsApi.Subscriptions>) {
        handleRequest(GetSubscriptionsToAccountHandler::class, request, responseObserver)
    }

    private inner class HandlerNotFoundException(message: String) : Exception(message)
}
