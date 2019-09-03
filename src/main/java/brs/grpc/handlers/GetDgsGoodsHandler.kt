package brs.grpc.handlers

import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.DGSGoodsStoreService

class GetDgsGoodsHandler(private val digitalGoodsStoreService: DGSGoodsStoreService) : GrpcApiHandler<BrsApi.GetDgsGoodsRequest, BrsApi.DgsGoods> {

    @Throws(Exception::class)
    override fun handleRequest(request: BrsApi.GetDgsGoodsRequest): BrsApi.DgsGoods {
        val sellerId = request.seller
        val inStockOnly = request.inStockOnly
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex
        val builder = BrsApi.DgsGoods.newBuilder()
        if (sellerId == 0L) {
            (if (inStockOnly) digitalGoodsStoreService.getGoodsInStock(firstIndex, lastIndex) else digitalGoodsStoreService.getAllGoods(firstIndex, lastIndex))
                    .forEach { goods -> builder.addGoods(ProtoBuilder.buildGoods(goods)) }
        } else {
            digitalGoodsStoreService.getSellerGoods(sellerId, inStockOnly, firstIndex, lastIndex)
                    .forEach { goods -> builder.addGoods(ProtoBuilder.buildGoods(goods)) }
        }
        return builder.build()
    }
}
