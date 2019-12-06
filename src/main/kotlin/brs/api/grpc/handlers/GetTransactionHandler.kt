package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ApiException
import brs.api.grpc.service.ProtoBuilder
import brs.entity.DependencyProvider
import brs.entity.Transaction

class GetTransactionHandler(private val dp: DependencyProvider) : GrpcApiHandler<BrsApi.GetTransactionRequest, BrsApi.Transaction> {
    override fun handleRequest(request: BrsApi.GetTransactionRequest): BrsApi.Transaction {
        return ProtoBuilder.buildTransaction(
            getTransaction(dp, request),
            dp.blockchainService.height
        )
    }

    companion object {
        fun getTransaction(dp: DependencyProvider, request: BrsApi.GetTransactionRequest): Transaction {
            val id = request.transactionId
            val fullHash = request.fullHash.toByteArray()
            var transaction: Transaction?
            if (fullHash.isNotEmpty()) {
                transaction = dp.blockchainService.getTransactionByFullHash(fullHash)
            } else if (id != 0L) {
                transaction = dp.blockchainService.getTransaction(id)
                if (transaction == null) transaction = dp.unconfirmedTransactionService.get(id)
            } else {
                throw ApiException("Could not find transaction")
            }
            if (transaction == null) {
                throw ApiException("Could not find transaction")
            }
            return transaction
        }
    }
}
