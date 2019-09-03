package brs.grpc.handlers

import brs.Constants
import brs.Genesis
import brs.TransactionType
import brs.fluxcapacitor.FluxCapacitor
import brs.fluxcapacitor.FluxValues
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import com.google.protobuf.Empty

import java.util.ArrayList
import java.util.stream.Collectors

class GetConstantsHandler(fluxCapacitor: FluxCapacitor) : GrpcApiHandler<Empty, BrsApi.Constants> {

    private val constants: BrsApi.Constants

    init {
        val transactionTypes = mutableListOf<BrsApi.Constants.TransactionType>()
        TransactionType.transactionTypes
                .forEach { (key, value) ->
                    transactionTypes
                            .add(BrsApi.Constants.TransactionType.newBuilder()
                                    .setType(key.toInt())
                                    .setDescription(TransactionType.getTypeDescription(key))
                                    .addAllSubtypes(value.entries
                                            .map { entry ->
                                                BrsApi.Constants.TransactionType.TransactionSubtype.newBuilder()
                                                        .setSubtype(entry.key.toInt())
                                                        .setDescription(entry.value.description)
                                                        .build()
                                            })
                                    .build())
                }

        this.constants = BrsApi.Constants.newBuilder()
                .setGenesisBlock(Genesis.GENESIS_BLOCK_ID)
                .setGenesisAccount(Genesis.CREATOR_ID)
                .setMaxBlockPayloadLength(fluxCapacitor.getValue(FluxValues.MAX_PAYLOAD_LENGTH))
                .setMaxArbitraryMessageLength(Constants.MAX_ARBITRARY_MESSAGE_LENGTH)
                .addAllTransactionTypes(transactionTypes)
                .build()
    }

    @Throws(Exception::class)
    override fun handleRequest(empty: Empty): BrsApi.Constants {
        return constants
    }
}
