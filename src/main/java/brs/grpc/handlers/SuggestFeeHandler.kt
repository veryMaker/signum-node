package brs.grpc.handlers

import brs.feesuggestions.FeeSuggestion
import brs.feesuggestions.FeeSuggestionCalculator
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import com.google.protobuf.Empty

class SuggestFeeHandler(private val feeSuggestionCalculator: FeeSuggestionCalculator) : GrpcApiHandler<Empty, BrsApi.FeeSuggestion> {

    override suspend fun handleRequest(empty: Empty): BrsApi.FeeSuggestion {
        val feeSuggestion = feeSuggestionCalculator.giveFeeSuggestion()
        return BrsApi.FeeSuggestion.newBuilder()
                .setCheap(feeSuggestion.cheapFee)
                .setStandard(feeSuggestion.standardFee)
                .setPriority(feeSuggestion.priorityFee)
                .build()
    }
}
