package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.ApiException
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.proto.ProtoBuilder
import brs.services.DigitalGoodsStoreService

class GetDgsGoodHandler(private val digitalGoodsStoreService: DigitalGoodsStoreService) : GrpcApiHandler<BrsApi.GetByIdRequest, BrsApi.DgsGood> {

    override fun handleRequest(request: BrsApi.GetByIdRequest): BrsApi.DgsGood {
        val goodsId = request.id
        val goods = digitalGoodsStoreService.getGoods(goodsId) ?: throw ApiException("Could not find goods")
        return ProtoBuilder.buildGoods(goods)
    }
}
