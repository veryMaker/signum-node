package brs.grpc.handlers

import brs.assetexchange.AssetExchange
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder

class GetAssetsByIssuerHandler(private val assetExchange: AssetExchange) : GrpcApiHandler<BrsApi.GetAccountRequest, BrsApi.Assets> {

    override fun handleRequest(getAccountRequest: BrsApi.GetAccountRequest): BrsApi.Assets {
        val builder = BrsApi.Assets.newBuilder()
        assetExchange.getAssetsIssuedBy(getAccountRequest.accountId, 0, -1)
                .forEach { asset -> builder.addAssets(ProtoBuilder.buildAsset(assetExchange, asset)) }
        return builder.build()
    }
}
