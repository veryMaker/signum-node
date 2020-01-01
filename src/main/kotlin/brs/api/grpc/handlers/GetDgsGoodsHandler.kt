package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ProtoBuilder
import brs.services.DigitalGoodsStoreService

class GetDgsGoodsHandler(private val digitalGoodsStoreService: DigitalGoodsStoreService) :
    GrpcApiHandler<BrsApi.GetDgsGoodsRequest, BrsApi.DgsGoods> {
    override fun handleRequest(request: BrsApi.GetDgsGoodsRequest): BrsApi.DgsGoods {
        val sellerId = request.seller
        val inStockOnly = request.inStockOnly
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex
        val builder = BrsApi.DgsGoods.newBuilder()
        if (sellerId == 0L) {
            (if (inStockOnly) digitalGoodsStoreService.getGoodsInStock(
                firstIndex,
                lastIndex
            ) else digitalGoodsStoreService.getAllGoods(firstIndex, lastIndex))
                .forEach { goods -> builder.addGoods(ProtoBuilder.buildGoods(goods)) }
        } else {
            digitalGoodsStoreService.getSellerGoods(sellerId, inStockOnly, firstIndex, lastIndex)
                .forEach { goods -> builder.addGoods(ProtoBuilder.buildGoods(goods)) }
        }
        return builder.build()
    }
}
