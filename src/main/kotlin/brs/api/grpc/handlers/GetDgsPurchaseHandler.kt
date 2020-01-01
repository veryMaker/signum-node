package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ApiException
import brs.api.grpc.service.ProtoBuilder
import brs.services.DigitalGoodsStoreService

class GetDgsPurchaseHandler(private val digitalGoodsStoreService: DigitalGoodsStoreService) :
    GrpcApiHandler<BrsApi.GetByIdRequest, BrsApi.DgsPurchase> {
    override fun handleRequest(request: BrsApi.GetByIdRequest): BrsApi.DgsPurchase {
        val purchase = digitalGoodsStoreService.getPurchase(request.id) ?: throw ApiException("Could not find purchase")
        return ProtoBuilder.buildPurchase(purchase, digitalGoodsStoreService.getGoods(purchase.goodsId)!!)
    }
}
