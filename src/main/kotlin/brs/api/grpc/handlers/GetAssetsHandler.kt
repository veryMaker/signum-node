package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ProtoBuilder
import brs.services.AssetExchangeService

class GetAssetsHandler(private val assetExchangeService: AssetExchangeService) :
    GrpcApiHandler<BrsApi.GetAssetsRequest, BrsApi.Assets> {
    override fun handleRequest(request: BrsApi.GetAssetsRequest): BrsApi.Assets {
        val builder = BrsApi.Assets.newBuilder()
        request.assetList.forEach { assetId ->
            val asset = assetExchangeService.getAsset(assetId!!) ?: return@forEach
            builder.addAssets(ProtoBuilder.buildAsset(assetExchangeService, asset))
        }
        return builder.build()
    }
}
