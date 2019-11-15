package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ApiException
import brs.api.grpc.service.ProtoBuilder
import brs.services.AssetExchangeService

class GetAssetHandler(private val assetExchangeService: AssetExchangeService) :
    GrpcApiHandler<BrsApi.GetByIdRequest, BrsApi.Asset> {

    override fun handleRequest(getByIdRequest: BrsApi.GetByIdRequest): BrsApi.Asset {
        val asset = assetExchangeService.getAsset(getByIdRequest.id) ?: throw ApiException("Could not find asset")
        return ProtoBuilder.buildAsset(assetExchangeService, asset)
    }
}
