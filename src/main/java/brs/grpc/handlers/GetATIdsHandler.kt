package brs.grpc.handlers

import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.services.ATService
import com.google.protobuf.Empty

class GetATIdsHandler(private val atService: ATService) : GrpcApiHandler<Empty, BrsApi.ATIds> {

    override fun handleRequest(empty: Empty): BrsApi.ATIds {
        return BrsApi.ATIds.newBuilder()
                .addAllIds(atService.allATIds)
                .build()
    }
}
