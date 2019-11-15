package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ApiException
import brs.api.grpc.service.ProtoBuilder
import brs.entity.Transaction
import brs.services.BlockchainService
import brs.services.TransactionProcessorService

class GetTransactionHandler(
    private val blockchainService: BlockchainService,
    private val transactionProcessorService: TransactionProcessorService
) : GrpcApiHandler<BrsApi.GetTransactionRequest, BrsApi.Transaction> {

    override fun handleRequest(request: BrsApi.GetTransactionRequest): BrsApi.Transaction {
        return ProtoBuilder.buildTransaction(
            getTransaction(blockchainService, transactionProcessorService, request),
            blockchainService.height
        )
    }

    companion object {
        fun getTransaction(
            blockchainService: BlockchainService,
            transactionProcessorService: TransactionProcessorService,
            request: BrsApi.GetTransactionRequest
        ): Transaction {
            val id = request.transactionId
            val fullHash = request.fullHash.toByteArray()
            var transaction: Transaction?
            if (fullHash.isNotEmpty()) {
                transaction = blockchainService.getTransactionByFullHash(fullHash)
            } else if (id != 0L) {
                transaction = blockchainService.getTransaction(id)
                if (transaction == null) transaction = transactionProcessorService.getUnconfirmedTransaction(id)
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
