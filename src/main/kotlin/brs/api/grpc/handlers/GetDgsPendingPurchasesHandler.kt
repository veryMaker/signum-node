package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.service.ApiException
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ProtoBuilder
import brs.services.DigitalGoodsStoreService

class GetDgsPendingPurchasesHandler(private val digitalGoodsStoreService: DigitalGoodsStoreService) :
    GrpcApiHandler<BrsApi.GetDgsPendingPurchasesRequest, BrsApi.DgsPurchases> {

    override fun handleRequest(request: BrsApi.GetDgsPendingPurchasesRequest): BrsApi.DgsPurchases {
        val sellerId = request.seller
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex
        if (sellerId == 0L) throw ApiException("Seller ID not set")
        val builder = BrsApi.DgsPurchases.newBuilder()
        digitalGoodsStoreService.getPendingSellerPurchases(sellerId, firstIndex, lastIndex)
            .forEach { purchase ->
                builder.addDgsPurchases(
                    ProtoBuilder.buildPurchase(
                        purchase,
                        digitalGoodsStoreService.getGoods(purchase.goodsId)!!
                    )
                )
            }
        return builder.build()
    }
}
