package brs.grpc

import com.google.protobuf.Empty
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GetMiningInfoHandlerTest : AbstractGrpcTest() {
    @Before
    fun setUpGetMiningInfoHandlerTest() {
        defaultBrsService()
    }

    @Test
    fun testGetMiningInfo() {
        runAndCancel {
            val miningInfoIterator = brsService.getMiningInfo(Empty.getDefaultInstance())
            assertTrue("Mining info is not available", miningInfoIterator.hasNext())
            val miningInfo = miningInfoIterator.next()
            assertNotNull("Mining info is null", miningInfo)
            assertEquals(1, miningInfo.height.toLong())
            assertArrayEquals(ByteArray(32), miningInfo.generationSignature.toByteArray())
            assertEquals(0, miningInfo.baseTarget)
        }
    }
}
