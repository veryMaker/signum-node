package brs.grpc.handlers

import brs.Asset
import brs.assetexchange.AssetExchange
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder

class GetAssetBalancesHandler(private val assetExchange: AssetExchange) : GrpcApiHandler<BrsApi.GetAssetBalancesRequest, BrsApi.AssetBalances> {

    @Throws(Exception::class)
    override fun handleRequest(request: BrsApi.GetAssetBalancesRequest): BrsApi.AssetBalances {
        val assetId = request.asset
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex
        val height = request.height

        val asset = assetExchange.getAsset(assetId) ?: throw ApiException("Could not find asset")

        val builder = BrsApi.AssetBalances.newBuilder()

        assetExchange.getAccountAssetsOverview(asset.id, height, firstIndex, lastIndex)
                .forEach { assetAccount -> builder.addAssetBalances(ProtoBuilder.buildAssetBalance(assetAccount)) }

        return builder.build()
    }
}
