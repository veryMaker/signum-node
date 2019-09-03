package brs.grpc.handlers

import brs.at.AT
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.ATService
import brs.services.AccountService

class GetATHandler(private val atService: ATService, private val accountService: AccountService) : GrpcApiHandler<BrsApi.GetByIdRequest, BrsApi.AT> {

    @Throws(Exception::class)
    override fun handleRequest(getATRequest: BrsApi.GetByIdRequest): BrsApi.AT {
        val at = atService.getAT(getATRequest.id) ?: throw ApiException("AT not found")
        return ProtoBuilder.buildAT(accountService, at)
    }
}
