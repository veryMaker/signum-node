package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ProtoBuilder
import brs.services.AliasService
import brs.util.FilteringIterator

class GetAliasesHandler(private val aliasService: AliasService) :
    GrpcApiHandler<BrsApi.GetAliasesRequest, BrsApi.Aliases> {
    override fun handleRequest(request: BrsApi.GetAliasesRequest): BrsApi.Aliases {
        val timestamp = request.timestamp
        val accountId = request.owner
        val firstIndex = request.indexRange.firstIndex
        val lastIndex = request.indexRange.lastIndex
        val aliases = BrsApi.Aliases.newBuilder()
        val aliasIterator = FilteringIterator(
            aliasService.getAliasesByOwner(accountId, 0, -1),
            { alias -> alias.timestamp >= timestamp },
            firstIndex,
            lastIndex
        )
        while (aliasIterator.hasNext()) {
            val alias = aliasIterator.next()
            val offer = aliasService.getOffer(alias)
            aliases.addAliases(ProtoBuilder.buildAlias(alias, offer))
        }
        return aliases.build()
    }
}
