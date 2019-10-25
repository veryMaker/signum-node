package brs.api.grpc.handlers
import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ProtoBuilder
import brs.entity.Purchase
import brs.services.DigitalGoodsStoreService
import brs.util.FilteringIterator

class GetDgsPurchasesHandler(private val digitalGoodsStoreService: DigitalGoodsStoreService) : GrpcApiHandler<BrsApi.GetDgsPurchasesRequest, BrsApi.DgsPurchases> {

    override fun handleRequest(request: BrsApi.GetDgsPurchasesRequest): BrsApi.DgsPurchases {
        val sellerId = request.seller
        val buyerId = request.buyer
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex
        val completed = request.completed


        val purchases: Collection<Purchase>
        purchases = when {
            sellerId == 0L && buyerId == 0L -> digitalGoodsStoreService.getAllPurchases(firstIndex, lastIndex)
            sellerId != 0L && buyerId == 0L -> digitalGoodsStoreService.getSellerPurchases(sellerId, firstIndex, lastIndex)
            sellerId == 0L -> digitalGoodsStoreService.getBuyerPurchases(buyerId, firstIndex, lastIndex)
            else -> digitalGoodsStoreService.getSellerBuyerPurchases(sellerId, buyerId, firstIndex, lastIndex)
        }

        val builder = BrsApi.DgsPurchases.newBuilder()
        FilteringIterator(purchases, { purchase -> !(completed && purchase.isPending) }, firstIndex, lastIndex)
                .forEach { purchase -> builder.addDgsPurchases(ProtoBuilder.buildPurchase(purchase, digitalGoodsStoreService.getGoods(purchase.goodsId)!!)) }
        return builder.build()
    }
}
