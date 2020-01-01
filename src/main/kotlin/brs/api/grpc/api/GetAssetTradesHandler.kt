package brs.api.grpc.api

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.ApiException
import brs.api.grpc.ProtoBuilder
import brs.entity.Trade
import brs.services.AssetExchangeService

class GetAssetTradesHandler(private val assetExchangeService: AssetExchangeService) :
    GrpcApiHandler<BrsApi.GetAssetTransfersRequest, BrsApi.AssetTrades> {
    override fun handleRequest(request: BrsApi.GetAssetTransfersRequest): BrsApi.AssetTrades {
        val accountId = request.account
        val assetId = request.asset
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex
        val trades: Collection<Trade>
        val asset = assetExchangeService.getAsset(assetId)
        trades = when {
            accountId == 0L -> assetExchangeService.getTrades(assetId, firstIndex, lastIndex)
            assetId == 0L -> assetExchangeService.getAccountTrades(accountId, firstIndex, lastIndex)
            else -> assetExchangeService.getAccountAssetTrades(accountId, assetId, firstIndex, lastIndex)
        }
        val builder = BrsApi.AssetTrades.newBuilder()
        trades.forEach { trade ->
            builder.addTrades(
                ProtoBuilder.buildTrade(
                    trade, asset ?: assetExchangeService.getAsset(trade.assetId) ?: throw ApiException(
                        "Asset not found"
                    )
                )
            )
        }
        return builder.build()
    }
}
