package brs.grpc.handlers

import brs.assetexchange.AssetExchange
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder

class GetOrdersHandler(private val assetExchange: AssetExchange) : GrpcApiHandler<BrsApi.GetOrdersRequest, BrsApi.Orders> {

    @Throws(Exception::class)
    override fun handleRequest(request: BrsApi.GetOrdersRequest): BrsApi.Orders {
        val builder = BrsApi.Orders.newBuilder()
        val assetId = request.asset
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex
        if (assetId == 0L) {
            // Get all open orders
            when (request.orderType) {
                BrsApi.OrderType.ASK -> assetExchange.getAllAskOrders(firstIndex, lastIndex)
                        .forEach { order -> builder.addOrders(ProtoBuilder.buildOrder(order)) }
                BrsApi.OrderType.BID -> assetExchange.getAllAskOrders(firstIndex, lastIndex)
                        .forEach { order -> builder.addOrders(ProtoBuilder.buildOrder(order)) }
                else -> throw ApiException("Order type unset")
            }
        } else {
            // Get orders under that asset
            when (request.orderType) {
                BrsApi.OrderType.ASK -> assetExchange.getSortedAskOrders(assetId, firstIndex, lastIndex)
                        .forEach { order -> builder.addOrders(ProtoBuilder.buildOrder(order)) }
                BrsApi.OrderType.BID -> assetExchange.getSortedBidOrders(assetId, firstIndex, lastIndex)
                        .forEach { order -> builder.addOrders(ProtoBuilder.buildOrder(order)) }
                else -> throw ApiException("Order type unset")
            }
        }
        return builder.build()
    }
}
