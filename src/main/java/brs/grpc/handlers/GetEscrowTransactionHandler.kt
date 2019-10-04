package brs.grpc.handlers

import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.EscrowService

class GetEscrowTransactionHandler(private val escrowService: EscrowService) : GrpcApiHandler<BrsApi.GetByIdRequest, BrsApi.EscrowTransaction> {

    override suspend fun handleRequest(request: BrsApi.GetByIdRequest): BrsApi.EscrowTransaction {
        val escrowId = request.id
        val escrow = escrowService.getEscrowTransaction(escrowId) ?: throw ApiException("Could not find escrow")
        return ProtoBuilder.buildEscrowTransaction(escrow)
    }
}
