package brs.peer

import brs.Block
import brs.Blockchain
import brs.Genesis
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers

import java.util.ArrayList

import org.junit.Assert.*

@RunWith(JUnit4::class)
class GetNextBlocksTest {
    private var getNextBlocks: GetNextBlocks? = null
    private var mockBlockchain: Blockchain? = null
    private var mockPeer: Peer? = null

    @Before
    fun setUpGetNextBlocksTest() {
        mockBlockchain = mock<Blockchain>()
        mockPeer = mock<Peer>()
        val mockBlock = mock<Block>()
        whenever(mockBlock.jsonObject).thenReturn(JsonObject())
        val blocks = ArrayList<Block>()
        for (i in 0..99) {
            blocks.add(mockBlock)
        }
        whenever(mockBlockchain!!.getBlocksAfter(eq(Genesis.GENESIS_BLOCK_ID), any())).thenReturn(blocks)
        getNextBlocks = GetNextBlocks(mockBlockchain)
    }

    @Test
    fun testGetNextBlocks() {
        val request = JsonObject()
        request.addProperty("blockId", java.lang.Long.toUnsignedString(Genesis.GENESIS_BLOCK_ID))
        val responseElement = getNextBlocks!!.processRequest(request, mockPeer)
        assertNotNull(responseElement)
        assertTrue(responseElement.isJsonObject)
        val response = responseElement.asJsonObject
        assertTrue(response.has("nextBlocks"))
        val nextBlocksElement = response.get("nextBlocks")
        assertNotNull(nextBlocksElement)
        assertTrue(nextBlocksElement.isJsonArray)
        val nextBlocks = nextBlocksElement.asJsonArray
        assertEquals(100, nextBlocks.size().toLong())
        for (nextBlock in nextBlocks) {
            assertNotNull(nextBlock)
            assertTrue(nextBlock.isJsonObject)
        }
    }

    @Test
    fun testGetNextBlocks_noIdSpecified() {
        val request = JsonObject()
        val responseElement = getNextBlocks!!.processRequest(request, mockPeer)
        assertNotNull(responseElement)
        assertTrue(responseElement is JsonObject)
        val response = responseElement.asJsonObject
        assertTrue(response.has("nextBlocks"))
        val nextBlocksElement = response.get("nextBlocks")
        assertNotNull(nextBlocksElement)
        assertTrue(nextBlocksElement is JsonArray)
        val nextBlocks = nextBlocksElement.asJsonArray
        assertEquals(0, nextBlocks.size().toLong())
    }
}
