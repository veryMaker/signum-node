package brs.api.grpc

import brs.api.grpc.proto.BrsApi
import io.grpc.StatusRuntimeException
import org.junit.Before
import org.junit.Test

class GetBlockHandlerTest : AbstractGrpcTest() {
    @Before
    fun setupGetBlockHandlerTest() {
        defaultBrsService()
    }

    @Test(expected = StatusRuntimeException::class)
    fun testGetBlockWithNoBlockSelected() {
        brsService.getBlock(BrsApi.GetBlockRequest.newBuilder()
                .setHeight(Integer.MAX_VALUE)
                .build())
    }
}
