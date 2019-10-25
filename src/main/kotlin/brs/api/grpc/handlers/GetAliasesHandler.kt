package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.proto.ProtoBuilder
import brs.services.AliasService
import brs.util.FilteringIterator

class GetAliasesHandler(private val aliasService: AliasService) : GrpcApiHandler<BrsApi.GetAliasesRequest, BrsApi.Aliases> {

    override fun handleRequest(getAliasesRequest: BrsApi.GetAliasesRequest): BrsApi.Aliases {
        val timestamp = getAliasesRequest.timestamp
        val accountId = getAliasesRequest.owner
        val firstIndex = getAliasesRequest.indexRange.firstIndex
        val lastIndex = getAliasesRequest.indexRange.lastIndex
        val aliases = BrsApi.Aliases.newBuilder()
        val aliasIterator = FilteringIterator(aliasService.getAliasesByOwner(accountId, 0, -1), { alias -> alias.timestamp >= timestamp }, firstIndex, lastIndex)
        while (aliasIterator.hasNext()) {
            val alias = aliasIterator.next()
            val offer = aliasService.getOffer(alias)
            aliases.addAliases(ProtoBuilder.buildAlias(alias, offer))
        }
        return aliases.build()
    }
}
