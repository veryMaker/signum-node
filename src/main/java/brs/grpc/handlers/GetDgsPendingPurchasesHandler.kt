package brs.grpc.handlers

import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.DGSGoodsStoreService

class GetDgsPendingPurchasesHandler(private val digitalGoodsStoreService: DGSGoodsStoreService) : GrpcApiHandler<BrsApi.GetDgsPendingPurchasesRequest, BrsApi.DgsPurchases> {

    override suspend fun handleRequest(request: BrsApi.GetDgsPendingPurchasesRequest): BrsApi.DgsPurchases {
        val sellerId = request.seller
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex
        if (sellerId == 0L) throw ApiException("Seller ID not set")
        val builder = BrsApi.DgsPurchases.newBuilder()
        digitalGoodsStoreService.getPendingSellerPurchases(sellerId, firstIndex, lastIndex)
                .forEach { purchase -> builder.addDgsPurchases(ProtoBuilder.buildPurchase(purchase, digitalGoodsStoreService.getGoods(purchase.goodsId)!!)) }
        return builder.build()
    }
}
