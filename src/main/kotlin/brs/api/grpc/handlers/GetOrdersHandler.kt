package brs.api.grpc.handlers

import brs.services.AssetExchangeService
import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.ApiException
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.proto.ProtoBuilder

class GetOrdersHandler(private val assetExchangeService: AssetExchangeService) : GrpcApiHandler<BrsApi.GetOrdersRequest, BrsApi.Orders> {

    override fun handleRequest(request: BrsApi.GetOrdersRequest): BrsApi.Orders {
        val builder = BrsApi.Orders.newBuilder()
        val assetId = request.asset
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex
        if (assetId == 0L) {
            // Get all open orders
            when (request.orderType) {
                BrsApi.OrderType.ASK -> assetExchangeService.getAllAskOrders(firstIndex, lastIndex)
                        .forEach { order -> builder.addOrders(ProtoBuilder.buildOrder(order)) }
                BrsApi.OrderType.BID -> assetExchangeService.getAllAskOrders(firstIndex, lastIndex)
                        .forEach { order -> builder.addOrders(ProtoBuilder.buildOrder(order)) }
                else -> throw ApiException("Order type unset")
            }
        } else {
            // Get orders under that asset
            when (request.orderType) {
                BrsApi.OrderType.ASK -> assetExchangeService.getSortedAskOrders(assetId, firstIndex, lastIndex)
                        .forEach { order -> builder.addOrders(ProtoBuilder.buildOrder(order)) }
                BrsApi.OrderType.BID -> assetExchangeService.getSortedBidOrders(assetId, firstIndex, lastIndex)
                        .forEach { order -> builder.addOrders(ProtoBuilder.buildOrder(order)) }
                else -> throw ApiException("Order type unset")
            }
        }
        return builder.build()
    }
}
