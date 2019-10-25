package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ProtoBuilder
import brs.services.EscrowService

class GetAccountEscrowTransactionsHandler(private val escrowService: EscrowService) : GrpcApiHandler<BrsApi.GetAccountRequest, BrsApi.EscrowTransactions> {
    override fun handleRequest(request: BrsApi.GetAccountRequest): BrsApi.EscrowTransactions {
        val accountId = request.accountId
        val builder = BrsApi.EscrowTransactions.newBuilder()
        escrowService.getEscrowTransactionsByParticipant(accountId)
                .forEach { escrow -> builder.addEscrowTransactions(ProtoBuilder.buildEscrowTransaction(escrow)) }
        return builder.build()
    }
}
