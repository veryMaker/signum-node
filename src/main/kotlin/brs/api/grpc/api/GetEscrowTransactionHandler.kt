package brs.api.grpc.api

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.ApiException
import brs.api.grpc.ProtoBuilder
import brs.services.EscrowService

class GetEscrowTransactionHandler(private val escrowService: EscrowService) :
    GrpcApiHandler<BrsApi.GetByIdRequest, BrsApi.EscrowTransaction> {
    override fun handleRequest(request: BrsApi.GetByIdRequest): BrsApi.EscrowTransaction {
        val escrowId = request.id
        val escrow = escrowService.getEscrowTransaction(escrowId) ?: throw ApiException(
            "Could not find escrow"
        )
        return ProtoBuilder.buildEscrowTransaction(escrow)
    }
}
