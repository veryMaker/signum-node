package brs.api.grpc.http

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.ProtoBuilder
import brs.services.AssetExchangeService

class GetAssetsByIssuerHandler(private val assetExchangeService: AssetExchangeService) :
    GrpcApiHandler<BrsApi.GetAccountRequest, BrsApi.Assets> {
    override fun handleRequest(request: BrsApi.GetAccountRequest): BrsApi.Assets {
        val builder = BrsApi.Assets.newBuilder()
        assetExchangeService.getAssetsIssuedBy(request.accountId, 0, -1)
            .forEach { asset -> builder.addAssets(ProtoBuilder.buildAsset(assetExchangeService, asset)) }
        return builder.build()
    }
}
