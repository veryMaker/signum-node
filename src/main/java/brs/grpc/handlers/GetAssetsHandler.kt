package brs.grpc.handlers

import brs.assetexchange.AssetExchange
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder

class GetAssetsHandler(private val assetExchange: AssetExchange) : GrpcApiHandler<BrsApi.GetAssetsRequest, BrsApi.Assets> {

    override suspend fun handleRequest(request: BrsApi.GetAssetsRequest): BrsApi.Assets {
        val builder = BrsApi.Assets.newBuilder()
        request.assetList.forEach { assetId ->
            val asset = assetExchange.getAsset(assetId!!) ?: return@forEach
            builder.addAssets(ProtoBuilder.buildAsset(assetExchange, asset))
        }
        return builder.build()
    }
}
