package brs.grpc.handlers

import brs.Asset
import brs.assetexchange.AssetExchange
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder

class GetAssetsHandler(private val assetExchange: AssetExchange) : GrpcApiHandler<BrsApi.GetAssetsRequest, BrsApi.Assets> {

    @Throws(Exception::class)
    override fun handleRequest(getAssetsRequest: BrsApi.GetAssetsRequest): BrsApi.Assets {
        val builder = BrsApi.Assets.newBuilder()
        getAssetsRequest.assetList.forEach { assetId ->
            val asset = assetExchange.getAsset(assetId!!)
            if (asset == null) return@getAssetsRequest.getAssetList().forEach
            builder.addAssets(ProtoBuilder.buildAsset(assetExchange, asset!!))
        }
        return builder.build()
    }
}
