package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ProtoBuilder
import brs.services.SubscriptionService

class GetSubscriptionsToAccountHandler(private val subscriptionService: SubscriptionService) :
    GrpcApiHandler<BrsApi.GetAccountRequest, BrsApi.Subscriptions> {
    override fun handleRequest(request: BrsApi.GetAccountRequest): BrsApi.Subscriptions {
        val accountId = request.accountId
        val builder = BrsApi.Subscriptions.newBuilder()
        subscriptionService.getSubscriptionsToId(accountId)
            .forEach { subscription -> builder.addSubscriptions(ProtoBuilder.buildSubscription(subscription)) }
        return builder.build()
    }
}
