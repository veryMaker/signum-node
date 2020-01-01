package brs.api.grpc.api

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.ApiException
import brs.api.grpc.ProtoBuilder
import brs.services.DigitalGoodsStoreService

class GetDgsPurchaseHandler(private val digitalGoodsStoreService: DigitalGoodsStoreService) :
    GrpcApiHandler<BrsApi.GetByIdRequest, BrsApi.DgsPurchase> {
    override fun handleRequest(request: BrsApi.GetByIdRequest): BrsApi.DgsPurchase {
        val purchase = digitalGoodsStoreService.getPurchase(request.id) ?: throw ApiException(
            "Could not find purchase"
        )
        return ProtoBuilder.buildPurchase(purchase, digitalGoodsStoreService.getGoods(purchase.goodsId)!!)
    }
}
