package brs.grpc.handlers

import brs.Blockchain
import brs.Transaction
import brs.TransactionProcessor
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.util.toHexString

class GetTransactionHandler(private val blockchain: Blockchain, private val transactionProcessor: TransactionProcessor) : GrpcApiHandler<BrsApi.GetTransactionRequest, BrsApi.Transaction> {

    @Throws(Exception::class)
    override fun handleRequest(request: BrsApi.GetTransactionRequest): BrsApi.Transaction {
        return ProtoBuilder.buildTransaction(getTransaction(blockchain, transactionProcessor, request), blockchain.height)
    }

    companion object {

        @Throws(ApiException::class)
        fun getTransaction(blockchain: Blockchain, transactionProcessor: TransactionProcessor, request: BrsApi.GetTransactionRequest): Transaction {
            val id = request.transactionId
            val fullHash = request.fullHash.toByteArray()
            var transaction: Transaction?
            if (fullHash.size > 0) {
                transaction = blockchain.getTransactionByFullHash(fullHash.toHexString())
            } else if (id != 0L) {
                transaction = blockchain.getTransaction(id)
                if (transaction == null) transaction = transactionProcessor.getUnconfirmedTransaction(id)
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
