package brs.grpc.handlers

import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.SubscriptionService

class GetAccountSubscriptionsHandler(private val subscriptionService: SubscriptionService) : GrpcApiHandler<BrsApi.GetAccountRequest, BrsApi.Subscriptions> {

    @Throws(Exception::class)
    override fun handleRequest(request: BrsApi.GetAccountRequest): BrsApi.Subscriptions {
        val accountId = request.accountId
        val builder = BrsApi.Subscriptions.newBuilder()
        subscriptionService.getSubscriptionsByParticipant(accountId)
                .forEach { subscription -> builder.addSubscriptions(ProtoBuilder.buildSubscription(subscription)) }
        return builder.build()
    }
}
