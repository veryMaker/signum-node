package brs.peer

import brs.objects.Genesis
import brs.services.BlockchainService
import brs.util.convert.toUnsignedString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GetNextBlockIdsTest {
    private lateinit var getNextBlockIds: GetNextBlockIds
    private lateinit var mockBlockchainService: BlockchainService
    private lateinit var mockPeer: Peer

    @Before
    fun setUpGetNextBlocksTest() {
        mockBlockchainService = mockk()
        mockPeer = mockk()
        val blocks = mutableListOf<Long>()
        repeat(100) {
            blocks.add((it + 1).toLong())
        }
        every { mockBlockchainService.getBlockIdsAfter(any(), any()) } returns blocks
        getNextBlockIds = GetNextBlockIds(mockBlockchainService)
    }

    @Test
    fun testGetNextBlocks() {
        val request = JsonObject()
        request.addProperty("blockId", Genesis.GENESIS_BLOCK_ID.toUnsignedString())
        val responseElement = getNextBlockIds.processRequest(request, mockPeer)
        assertNotNull(responseElement)
        assertTrue(responseElement is JsonObject)
        val response = responseElement.asJsonObject
        assertTrue(response.has("nextBlockIds"))
        val nextBlocksElement = response.get("nextBlockIds")
        assertNotNull(nextBlocksElement)
        assertTrue(nextBlocksElement is JsonArray)
        val nextBlocks = nextBlocksElement.asJsonArray
        assertEquals(100, nextBlocks.size().toLong())
        nextBlocks.forEach { nextBlock ->
            assertNotNull(nextBlock)
            assertTrue(nextBlock.isJsonPrimitive)
        }
    }

    @Test
    fun testGetNextBlocks_noIdSpecified() {
        val request = JsonObject()
        val responseElement = getNextBlockIds.processRequest(request, mockPeer)
        assertNotNull(responseElement)
        assertTrue(responseElement is JsonObject)
        val response = responseElement.asJsonObject
        assertTrue(response.has("nextBlockIds"))
        val nextBlocksElement = response.get("nextBlockIds")
        assertNotNull(nextBlocksElement)
        assertTrue(nextBlocksElement is JsonArray)
        val nextBlocks = nextBlocksElement.asJsonArray
        assertEquals(0, nextBlocks.size().toLong())
    }

    @Test
    fun test_nothingProvided() {
        PeerApiTestUtils.testWithNothingProvided(getNextBlockIds)
    }
}
