package brs.grpc.handlers

import brs.Subscription
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.SubscriptionService

class GetSubscriptionHandler(private val subscriptionService: SubscriptionService) : GrpcApiHandler<BrsApi.GetByIdRequest, BrsApi.Subscription> {

    override suspend fun handleRequest(request: BrsApi.GetByIdRequest): BrsApi.Subscription {
        val subscriptionId = request.id
        val subscription = subscriptionService.getSubscription(subscriptionId)
                ?: throw ApiException("Could not find subscription")
        return ProtoBuilder.buildSubscription(subscription)
    }
}
