package brs.grpc.handlers

import brs.Asset
import brs.Trade
import brs.assetexchange.AssetExchange
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder

class GetAssetTradesHandler(private val assetExchange: AssetExchange) : GrpcApiHandler<BrsApi.GetAssetTransfersRequest, BrsApi.AssetTrades> {

    @Throws(Exception::class)
    override fun handleRequest(request: BrsApi.GetAssetTransfersRequest): BrsApi.AssetTrades {
        val accountId = request.account
        val assetId = request.asset
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex
        val trades: Collection<Trade>
        val asset = assetExchange.getAsset(assetId)
        if (accountId == 0L) {
            trades = assetExchange.getTrades(assetId, firstIndex, lastIndex)
        } else if (assetId == 0L) {
            trades = assetExchange.getAccountTrades(accountId, firstIndex, lastIndex)
        } else {
            trades = assetExchange.getAccountAssetTrades(accountId, assetId, firstIndex, lastIndex)
        }
        val builder = BrsApi.AssetTrades.newBuilder()
        trades.forEach { trade ->
            builder.addTrades(ProtoBuilder.buildTrade(trade, asset ?: assetExchange.getAsset(trade.assetId)))
        }
        return builder.build()
    }
}
