package brs.grpc.handlers

import brs.Account
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.AccountService

class GetAccountHandler(private val accountService: AccountService) : GrpcApiHandler<BrsApi.GetAccountRequest, BrsApi.Account> {

    @Throws(Exception::class)
    override fun handleRequest(request: BrsApi.GetAccountRequest): BrsApi.Account {
        val account: Account?
        try {
            account = accountService.getAccount(request.accountId)
            if (account == null) throw NullPointerException()
        } catch (e: RuntimeException) {
            throw ApiException("Could not find account")
        }

        return ProtoBuilder.buildAccount(account, accountService)
    }
}
