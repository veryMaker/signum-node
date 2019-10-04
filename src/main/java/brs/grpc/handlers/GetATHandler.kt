package brs.grpc.handlers

import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.ATService
import brs.services.AccountService

class GetATHandler(private val atService: ATService, private val accountService: AccountService) : GrpcApiHandler<BrsApi.GetByIdRequest, BrsApi.AT> {

    override suspend fun handleRequest(getATRequest: BrsApi.GetByIdRequest): BrsApi.AT {
        val at = atService.getAT(getATRequest.id) ?: throw ApiException("AT not found")
        return ProtoBuilder.buildAT(accountService, at)
    }
}
