package brs.api.grpc.http

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.ApiException
import brs.api.grpc.ProtoBuilder
import brs.entity.Order
import brs.services.AssetExchangeService

class GetOrderHandler(private val assetExchangeService: AssetExchangeService) :
    GrpcApiHandler<BrsApi.GetOrderRequest, BrsApi.Order> {
    override fun handleRequest(request: BrsApi.GetOrderRequest): BrsApi.Order {
        val order: Order = when (request.orderType) {
            BrsApi.AssetOrderType.ASK -> assetExchangeService.getAskOrder(request.orderId)
            BrsApi.AssetOrderType.BID -> assetExchangeService.getBidOrder(request.orderId)
            else -> throw ApiException("Order type unset")
        }
            ?: throw ApiException("Could not find order")
        return ProtoBuilder.buildOrder(order)
    }
}
