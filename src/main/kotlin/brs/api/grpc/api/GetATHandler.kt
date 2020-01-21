package brs.api.grpc.api

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.ApiException
import brs.api.grpc.ProtoBuilder
import brs.services.ATService
import brs.services.AccountService

class GetATHandler(private val atService: ATService, private val accountService: AccountService) :
    GrpcApiHandler<BrsApi.GetByIdRequest, BrsApi.AT> {
    override fun handleRequest(request: BrsApi.GetByIdRequest): BrsApi.AT {
        val at = atService.getAT(request.id) ?: throw ApiException("AT not found")
        return ProtoBuilder.buildAT(accountService, at)
    }
}
