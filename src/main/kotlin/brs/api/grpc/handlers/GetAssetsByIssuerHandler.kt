package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ProtoBuilder
import brs.services.AssetExchangeService

class GetAssetsByIssuerHandler(private val assetExchangeService: AssetExchangeService) :
    GrpcApiHandler<BrsApi.GetAccountRequest, BrsApi.Assets> {

    override fun handleRequest(getAccountRequest: BrsApi.GetAccountRequest): BrsApi.Assets {
        val builder = BrsApi.Assets.newBuilder()
        assetExchangeService.getAssetsIssuedBy(getAccountRequest.accountId, 0, -1)
            .forEach { asset -> builder.addAssets(ProtoBuilder.buildAsset(assetExchangeService, asset)) }
        return builder.build()
    }
}
