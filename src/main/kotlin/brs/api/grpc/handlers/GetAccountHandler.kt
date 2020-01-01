package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ApiException
import brs.api.grpc.service.ProtoBuilder
import brs.entity.Account
import brs.services.AccountService

class GetAccountHandler(private val accountService: AccountService) :
    GrpcApiHandler<BrsApi.GetAccountRequest, BrsApi.Account> {
    override fun handleRequest(request: BrsApi.GetAccountRequest): BrsApi.Account {
        val account: Account?
        try {
            account = accountService.getAccount(request.accountId)
            if (account == null) throw NullPointerException()
        } catch (e: Exception) {
            throw ApiException("Could not find account")
        }

        return ProtoBuilder.buildAccount(account, accountService)
    }
}
