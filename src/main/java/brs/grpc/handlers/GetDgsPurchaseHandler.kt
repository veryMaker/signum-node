package brs.grpc.handlers

import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.DGSGoodsStoreService

class GetDgsPurchaseHandler(private val digitalGoodsStoreService: DGSGoodsStoreService) : GrpcApiHandler<BrsApi.GetByIdRequest, BrsApi.DgsPurchase> {

    override suspend fun handleRequest(request: BrsApi.GetByIdRequest): BrsApi.DgsPurchase {
        val purchase = digitalGoodsStoreService.getPurchase(request.id) ?: throw ApiException("Could not find purchase")
        return ProtoBuilder.buildPurchase(purchase, digitalGoodsStoreService.getGoods(purchase.goodsId)!!)
    }
}
