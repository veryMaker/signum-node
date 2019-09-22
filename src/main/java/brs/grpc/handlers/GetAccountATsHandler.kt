package brs.grpc.handlers

import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.ATService
import brs.services.AccountService

class GetAccountATsHandler(private val atService: ATService, private val accountService: AccountService) : GrpcApiHandler<BrsApi.GetAccountRequest, BrsApi.AccountATs> {

    override fun handleRequest(request: BrsApi.GetAccountRequest): BrsApi.AccountATs {
        return BrsApi.AccountATs.newBuilder()
                .addAllAts(atService.getATsIssuedBy(request.accountId)
                        .map { atService.getAT(it)!! }
                        .map { at -> ProtoBuilder.buildAT(accountService, at) })
                .build()
    }
}
