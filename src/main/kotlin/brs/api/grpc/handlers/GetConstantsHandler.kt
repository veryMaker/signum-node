package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.entity.DependencyProvider
import brs.objects.Constants
import brs.objects.FluxValues
import brs.objects.Genesis
import brs.transaction.type.TransactionType
import com.google.protobuf.Empty

class GetConstantsHandler(dp: DependencyProvider) : GrpcApiHandler<Empty, BrsApi.Constants> {

    private val constants: BrsApi.Constants

    init {
        val transactionTypes = mutableListOf<BrsApi.Constants.TransactionType>()
        dp.transactionTypes.forEach { (key, value) ->
            transactionTypes
                .add(
                    BrsApi.Constants.TransactionType.newBuilder()
                        .setType(key.toInt())
                        .setDescription(TransactionType.getTypeDescription(key))
                        .addAllSubtypes(value.entries
                            .map { entry ->
                                BrsApi.Constants.TransactionType.TransactionSubtype.newBuilder()
                                    .setSubtype(entry.key.toInt())
                                    .setDescription(entry.value.description)
                                    .build()
                            })
                        .build()
                )
        }

        this.constants = BrsApi.Constants.newBuilder()
            .setGenesisBlock(Genesis.GENESIS_BLOCK_ID)
            .setGenesisAccount(Genesis.CREATOR_ID)
            .setMaxBlockPayloadLength(dp.fluxCapacitorService.getValue(FluxValues.MAX_PAYLOAD_LENGTH))
            .setMaxArbitraryMessageLength(Constants.MAX_ARBITRARY_MESSAGE_LENGTH)
            .addAllTransactionTypes(transactionTypes)
            .build()
    }

    override fun handleRequest(request: Empty): BrsApi.Constants {
        return constants
    }
}
