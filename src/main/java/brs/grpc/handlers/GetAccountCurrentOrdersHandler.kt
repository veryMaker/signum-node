package brs.grpc.handlers

import brs.assetexchange.AssetExchange
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder

class GetAccountCurrentOrdersHandler(private val assetExchange: AssetExchange) : GrpcApiHandler<BrsApi.GetAccountOrdersRequest, BrsApi.Orders> {

    @Throws(Exception::class)
    override fun handleRequest(request: BrsApi.GetAccountOrdersRequest): BrsApi.Orders {
        val accountId = request.account
        val assetId = request.asset
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex

        val builder = BrsApi.Orders.newBuilder()
        when (request.orderType) {
            BrsApi.OrderType.ASK -> (if (assetId == 0L) assetExchange.getAskOrdersByAccount(accountId, firstIndex, lastIndex) else assetExchange.getAskOrdersByAccountAsset(accountId, assetId, firstIndex, lastIndex))
                    .forEach { order -> builder.addOrders(ProtoBuilder.buildOrder(order)) }
            BrsApi.OrderType.BID -> (if (assetId == 0L) assetExchange.getBidOrdersByAccount(accountId, firstIndex, lastIndex) else assetExchange.getBidOrdersByAccountAsset(accountId, assetId, firstIndex, lastIndex))
                    .forEach { order -> builder.addOrders(ProtoBuilder.buildOrder(order)) }
            else -> throw ApiException("Order Type not set")
        }
        return builder.build()
    }
}
