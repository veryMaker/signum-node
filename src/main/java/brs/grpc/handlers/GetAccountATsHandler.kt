package brs.grpc.handlers

import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.ATService
import brs.services.AccountService

import java.util.stream.Collectors

class GetAccountATsHandler(private val atService: ATService, private val accountService: AccountService) : GrpcApiHandler<BrsApi.GetAccountRequest, BrsApi.AccountATs> {

    @Throws(Exception::class)
    override fun handleRequest(getAccountRequest: BrsApi.GetAccountRequest): BrsApi.AccountATs {
        return BrsApi.AccountATs.newBuilder()
                .addAllAts(atService.getATsIssuedBy(getAccountRequest.accountId)
                        .map { atService.getAT(it) }
                        .map { at -> ProtoBuilder.buildAT(accountService, at) })
                .build()
    }
}
