package brs.api.grpc.api

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.services.FeeSuggestionService
import com.google.protobuf.Empty

class SuggestFeeHandler(private val feeSuggestionServiceImpl: FeeSuggestionService) :
    GrpcApiHandler<Empty, BrsApi.FeeSuggestion> {
    override fun handleRequest(request: Empty): BrsApi.FeeSuggestion {
        val feeSuggestion = feeSuggestionServiceImpl.giveFeeSuggestion()
        return BrsApi.FeeSuggestion.newBuilder()
            .setCheap(feeSuggestion.cheapFee)
            .setStandard(feeSuggestion.standardFee)
            .setPriority(feeSuggestion.priorityFee)
            .build()
    }
}
