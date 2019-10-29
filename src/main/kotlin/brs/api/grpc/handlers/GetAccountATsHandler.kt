package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ProtoBuilder
import brs.services.ATService
import brs.services.AccountService

class GetAccountATsHandler(private val atService: ATService, private val accountService: AccountService) :
    GrpcApiHandler<BrsApi.GetAccountRequest, BrsApi.AccountATs> {
    override fun handleRequest(request: BrsApi.GetAccountRequest): BrsApi.AccountATs {
        return BrsApi.AccountATs.newBuilder()
            .addAllAts(atService.getATsIssuedBy(request.accountId)
                .map { atService.getAT(it)!! }
                .map { at -> ProtoBuilder.buildAT(accountService, at) })
            .build()
    }
}
