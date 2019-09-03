package brs.grpc.handlers

import brs.Account
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.AccountService
import java.util.Objects

class GetAccountsHandler(private val accountService: AccountService) : GrpcApiHandler<BrsApi.GetAccountsRequest, BrsApi.Accounts> {

    @Throws(Exception::class)
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
            accounts.forEach { assignment -> builder.addIds(assignment.getAccountId()) }
            if (request.includeAccounts) {
                accounts.forEach { assignment -> builder.addAccounts(ProtoBuilder.buildAccount(accountService.getAccount(assignment.getAccountId()), accountService)) }
            }
        }
        return builder.build()
    }
}
