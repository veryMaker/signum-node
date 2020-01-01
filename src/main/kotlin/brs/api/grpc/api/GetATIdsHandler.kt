package brs.api.grpc.api

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.services.ATService
import com.google.protobuf.Empty

class GetATIdsHandler(private val atService: ATService) : GrpcApiHandler<Empty, BrsApi.ATIds> {
    override fun handleRequest(request: Empty): BrsApi.ATIds {
        return BrsApi.ATIds.newBuilder()
            .addAllIds(atService.getAllATIds())
            .build()
    }
}
