package brs.grpc.handlers

import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.EscrowService

class GetAccountEscrowTransactionsHandler(private val escrowService: EscrowService) : GrpcApiHandler<BrsApi.GetAccountRequest, BrsApi.EscrowTransactions> {

    @Throws(Exception::class)
    override fun handleRequest(request: BrsApi.GetAccountRequest): BrsApi.EscrowTransactions {
        val accountId = request.accountId
        val builder = BrsApi.EscrowTransactions.newBuilder()
        escrowService.getEscrowTransactionsByParticipant(accountId)
                .forEach { escrow -> builder.addEscrowTransactions(ProtoBuilder.buildEscrowTransaction(escrow)) }
        return builder.build()
    }
}
