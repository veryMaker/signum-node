package brs.grpc.handlers

import brs.Attachment
import brs.Blockchain
import brs.TransactionProcessor
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.services.TimeService
import burst.kit.crypto.BurstCrypto
import com.google.protobuf.Any
import com.google.protobuf.InvalidProtocolBufferException

class CompleteBasicTransactionHandler(private val timeService: TimeService, private val transactionProcessor: TransactionProcessor, private val blockchain: Blockchain) : GrpcApiHandler<BrsApi.BasicTransaction, BrsApi.BasicTransaction> {

    @Throws(Exception::class)
    override fun handleRequest(basicTransaction: BrsApi.BasicTransaction): BrsApi.BasicTransaction {
        try {
            val builder = basicTransaction.toBuilder()
            val attachment = Attachment.AbstractAttachment.parseProtobufMessage(basicTransaction.attachment)
            if (builder.deadline == 0) {
                builder.deadline = 1440
            }
            if (builder.senderId == 0L) {
                builder.senderId = BurstCrypto.getInstance().getBurstAddressFromPublic(builder.senderPublicKey.toByteArray()).burstID.signedLongId
            }
            builder.version = transactionProcessor.getTransactionVersion(blockchain.height)
            builder.type = attachment.transactionType.type.toInt()
            builder.subtype = attachment.transactionType.subtype.toInt()
            builder.timestamp = timeService.epochTime
            if (builder.attachment == Any.getDefaultInstance()) {
                builder.attachment = Attachment.ORDINARY_PAYMENT.protobufMessage
            }
            return builder.build()
        } catch (e: InvalidProtocolBufferException) {
            throw ApiException("Could not parse an Any: " + e.message)
        }

    }
}
