package brs.api.grpc.handlers

import brs.services.AssetExchangeService
import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.service.ApiException
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ProtoBuilder

class GetAssetBalancesHandler(private val assetExchangeService: AssetExchangeService) : GrpcApiHandler<BrsApi.GetAssetBalancesRequest, BrsApi.AssetBalances> {

    override fun handleRequest(request: BrsApi.GetAssetBalancesRequest): BrsApi.AssetBalances {
        val assetId = request.asset
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex
        val height = request.height

        val asset = assetExchangeService.getAsset(assetId) ?: throw ApiException("Could not find asset")

        val builder = BrsApi.AssetBalances.newBuilder()

        assetExchangeService.getAccountAssetsOverview(asset.id, height, firstIndex, lastIndex)
                .forEach { assetAccount -> builder.addAssetBalances(ProtoBuilder.buildAssetBalance(assetAccount)) }

        return builder.build()
    }
}
