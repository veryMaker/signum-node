package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ProtoBuilder
import brs.services.AccountService

class GetAccountsHandler(private val accountService: AccountService) :
    GrpcApiHandler<BrsApi.GetAccountsRequest, BrsApi.Accounts> {
    override fun handleRequest(request: BrsApi.GetAccountsRequest): BrsApi.Accounts {
        val builder = BrsApi.Accounts.newBuilder()
        if (request.name != "") {
            val accounts = accountService.getAccountsWithName(request.name)
            accounts.forEach { account -> builder.addIds(account.id) }
            if (request.includeAccounts) {
                accounts.forEach { account -> builder.addAccounts(ProtoBuilder.buildAccount(account, accountService)) }
            }
        }
        if (request.rewardRecipient != 0L) {
            val accounts = accountService.getAccountsWithRewardRecipient(request.rewardRecipient)
            accounts.forEach { assignment -> builder.addIds(assignment.accountId) }
            if (request.includeAccounts) {
                accounts.forEach { assignment ->
                    builder.addAccounts(
                        ProtoBuilder.buildAccount(
                            accountService.getAccount(
                                assignment.accountId
                            ) ?: return@forEach, accountService
                        )
                    )
                }
            }
        }
        return builder.build()
    }
}
