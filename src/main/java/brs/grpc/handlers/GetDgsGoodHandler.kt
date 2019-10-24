package brs.grpc.handlers

import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.DGSGoodsStoreService

class GetDgsGoodHandler(private val digitalGoodsStoreService: DGSGoodsStoreService) : GrpcApiHandler<BrsApi.GetByIdRequest, BrsApi.DgsGood> {

    override fun handleRequest(request: BrsApi.GetByIdRequest): BrsApi.DgsGood {
        val goodsId = request.id
        val goods = digitalGoodsStoreService.getGoods(goodsId) ?: throw ApiException("Could not find goods")
        return ProtoBuilder.buildGoods(goods)
    }
}
