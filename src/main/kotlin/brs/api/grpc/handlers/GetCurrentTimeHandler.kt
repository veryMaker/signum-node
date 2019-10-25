package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.services.TimeService
import com.google.protobuf.Empty

class GetCurrentTimeHandler(private val timeService: TimeService) : GrpcApiHandler<Empty, BrsApi.Time> {

    override fun handleRequest(empty: Empty): BrsApi.Time {
        return BrsApi.Time.newBuilder()
                .setTime(timeService.epochTime)
                .build()
    }
}
