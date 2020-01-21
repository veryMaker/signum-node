package brs.api.grpc.api

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.ApiException
import brs.api.grpc.ProtoBuilder
import brs.services.AssetExchangeService

class GetAssetHandler(private val assetExchangeService: AssetExchangeService) :
    GrpcApiHandler<BrsApi.GetByIdRequest, BrsApi.Asset> {
    override fun handleRequest(request: BrsApi.GetByIdRequest): BrsApi.Asset {
        val asset = assetExchangeService.getAsset(request.id) ?: throw ApiException("Could not find asset")
        return ProtoBuilder.buildAsset(assetExchangeService, asset)
    }
}
