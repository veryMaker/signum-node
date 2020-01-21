package brs.api.grpc.api

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.ProtoBuilder
import brs.services.AliasService
import brs.util.misc.filterWithLimits

class GetAliasesHandler(private val aliasService: AliasService) :
    GrpcApiHandler<BrsApi.GetAliasesRequest, BrsApi.Aliases> {
    override fun handleRequest(request: BrsApi.GetAliasesRequest): BrsApi.Aliases {
        val timestamp = request.timestamp
        val accountId = request.owner
        val firstIndex = request.indexRange.firstIndex
        val lastIndex = request.indexRange.lastIndex
        val aliases = BrsApi.Aliases.newBuilder()
        aliasService.getAliasesByOwner(accountId, 0, -1)
            .filterWithLimits(firstIndex, lastIndex) { alias -> alias.timestamp >= timestamp }
            .forEach { alias ->
            val offer = aliasService.getOffer(alias)
            aliases.addAliases(ProtoBuilder.buildAlias(alias, offer))
        }
        return aliases.build()
    }
}
