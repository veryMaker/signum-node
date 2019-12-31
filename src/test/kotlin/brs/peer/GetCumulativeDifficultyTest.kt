package brs.peer

import brs.common.QuickMocker
import brs.entity.Block
import brs.services.BlockchainService
import brs.util.json.safeGetAsLong
import brs.util.json.safeGetAsString
import brs.util.jetty.get
import com.google.gson.JsonObject
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.math.BigInteger

class GetCumulativeDifficultyTest {

    private lateinit var t: GetCumulativeDifficulty
    private lateinit var mockLastBlock: Block
    private lateinit var mockBlockchainService: BlockchainService

    @Before
    fun setUp() {
        mockBlockchainService = mockk(relaxed = true)
        mockLastBlock = mockk(relaxed = true)
        every { mockLastBlock.height } returns 50
        every { mockLastBlock.cumulativeDifficulty } returns BigInteger.TEN
        every { mockBlockchainService.lastBlock } returns mockLastBlock
        t = GetCumulativeDifficulty(mockBlockchainService)
    }

    @Test
    fun processRequest() {

        val request = QuickMocker.jsonObject()

        val result = t.processRequest(request, mockk(relaxed = true)) as JsonObject
        assertNotNull(result)

        assertEquals("10", result.get("cumulativeDifficulty").safeGetAsString())
        assertEquals(50L, result.get("blockchainHeight").safeGetAsLong())
    }

    @Test
    fun test_nothingProvided() {
        PeerApiTestUtils.testWithNothingProvided(t)
    }
}
