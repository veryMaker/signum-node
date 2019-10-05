package brs.peer

import brs.Blockchain
import brs.Genesis
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GetNextBlockIdsTest {
    private lateinit var getNextBlockIds: GetNextBlockIds
    private lateinit var mockBlockchain: Blockchain
    private lateinit var mockPeer: Peer

    @Before
    fun setUpGetNextBlocksTest() {
        mockBlockchain = mock<Blockchain>()
        mockPeer = mock<Peer>()
        val blocks = mutableListOf<Long>()
        repeat(100) {
            blocks.add((it + 1).toLong())
        }
        whenever(mockBlockchain!!.getBlockIdsAfter(eq(Genesis.GENESIS_BLOCK_ID), any())).doReturn(blocks)
        getNextBlockIds = GetNextBlockIds(mockBlockchain!!)
    }

    @Test
    fun testGetNextBlocks() = runBlocking {
        val request = JsonObject()
        request.addProperty("blockId", java.lang.Long.toUnsignedString(Genesis.GENESIS_BLOCK_ID))
        val responseElement = getNextBlockIds!!.processRequest(request, mockPeer!!)
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
    fun testGetNextBlocks_noIdSpecified() = runBlocking {
        val request = JsonObject()
        val responseElement = getNextBlockIds!!.processRequest(request, mockPeer!!)
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
}
