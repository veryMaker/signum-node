package brs.grpc.handlers

import brs.DigitalGoodsStore
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.DGSGoodsStoreService
import brs.util.FilteringIterator

class GetDgsPurchasesHandler(private val digitalGoodsStoreService: DGSGoodsStoreService) : GrpcApiHandler<BrsApi.GetDgsPurchasesRequest, BrsApi.DgsPurchases> {

    override suspend fun handleRequest(request: BrsApi.GetDgsPurchasesRequest): BrsApi.DgsPurchases {
        val sellerId = request.seller
        val buyerId = request.buyer
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex
        val completed = request.completed


        val purchases: Collection<DigitalGoodsStore.Purchase>
        purchases = when {
            sellerId == 0L && buyerId == 0L -> digitalGoodsStoreService.getAllPurchases(firstIndex, lastIndex)
            sellerId != 0L && buyerId == 0L -> digitalGoodsStoreService.getSellerPurchases(sellerId, firstIndex, lastIndex)
            sellerId == 0L -> digitalGoodsStoreService.getBuyerPurchases(buyerId, firstIndex, lastIndex)
            else -> digitalGoodsStoreService.getSellerBuyerPurchases(sellerId, buyerId, firstIndex, lastIndex)
        }

        val builder = BrsApi.DgsPurchases.newBuilder()
        FilteringIterator(purchases, { purchase -> !(completed && purchase.isPending) }, firstIndex, lastIndex)
                .forEachRemaining { purchase -> builder.addDgsPurchases(ProtoBuilder.buildPurchase(purchase, digitalGoodsStoreService.getGoods(purchase.goodsId)!!)) }
        return builder.build()
    }
}
