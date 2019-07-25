package brs.grpc

import brs.grpc.proto.BrsApi
import com.google.protobuf.Empty
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import java.io.IOException

import org.junit.Assert.*

@RunWith(JUnit4::class)
class GetMiningInfoHandlerTest : AbstractGrpcTest() {
    @Before
    @Throws(IOException::class)
    fun setUpGetMiningInfoHandlerTest() {
        defaultBrsService()
    }

    @Test
    fun testGetMiningInfo() {
        runAndCancel {
            val miningInfoIterator = brsService!!.getMiningInfo(Empty.getDefaultInstance())
            assertTrue("Mining info is not available", miningInfoIterator.hasNext())
            val miningInfo = miningInfoIterator.next()
            assertNotNull("Mining info is null", miningInfo)
            assertEquals(1, miningInfo.getHeight().toLong())
            assertArrayEquals(ByteArray(32), miningInfo.getGenerationSignature().toByteArray())
            assertEquals(0, miningInfo.getBaseTarget())
        }
    }
}
