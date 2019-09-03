package brs.grpc.handlers

import brs.Alias
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.AliasService

class GetAliasHandler(private val aliasService: AliasService) : GrpcApiHandler<BrsApi.GetAliasRequest, BrsApi.Alias> {

    @Throws(Exception::class)
    override fun handleRequest(getAliasRequest: BrsApi.GetAliasRequest): BrsApi.Alias {
        val alias = (if (getAliasRequest.name == "") aliasService.getAlias(getAliasRequest.id) else aliasService.getAlias(getAliasRequest.name))
                ?: throw ApiException("Alias not found")
        return ProtoBuilder.buildAlias(alias, aliasService.getOffer(alias))
    }
}
