package brs.http

import brs.Block
import brs.Blockchain
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.http.JSONResponses.INCORRECT_BLOCK
import brs.http.JSONResponses.INCORRECT_HEIGHT
import brs.http.JSONResponses.INCORRECT_TIMESTAMP
import brs.http.JSONResponses.UNKNOWN_BLOCK
import brs.http.common.Parameters.BLOCK_PARAMETER
import brs.http.common.Parameters.HEIGHT_PARAMETER
import brs.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.services.BlockService
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetBlockTest {

    private lateinit var t: GetBlock

    private lateinit var blockchainMock: Blockchain
    private lateinit var blockServiceMock: BlockService

    @Before
    fun setUp() {
        blockchainMock = mock()
        blockServiceMock = mock()

        t = GetBlock(blockchainMock, blockServiceMock)
    }

    @Test
    fun processRequest_withBlockId() {
        val blockId = 2L

        val request = QuickMocker.httpServletRequest(
                MockParam(BLOCK_PARAMETER, blockId)
        )

        val mockBlock = mock<Block>()

        whenever(blockchainMock.getBlock(eq(blockId))).doReturn(mockBlock)

        val result = t.processRequest(request) as JsonObject

        assertNotNull(result)
    }

    @Test
    fun processRequest_withBlockId_incorrectBlock() {
        val request = QuickMocker.httpServletRequest(
                MockParam(BLOCK_PARAMETER, "notALong")
        )

        assertEquals(INCORRECT_BLOCK, t.processRequest(request))
    }

    @Test
    fun processRequest_withHeight() {
        val blockHeight = 2

        val request = QuickMocker.httpServletRequest(
                MockParam(HEIGHT_PARAMETER, blockHeight)
        )

        val mockBlock = mock<Block>()

        whenever(blockchainMock.height).doReturn(100)
        whenever(blockchainMock.getBlockAtHeight(eq(blockHeight))).doReturn(mockBlock)

        val result = t.processRequest(request) as JsonObject

        assertNotNull(result)
    }

    @Test
    fun processRequest_withHeight_incorrectHeight_unParsable() {
        val request = QuickMocker.httpServletRequest(
                MockParam(HEIGHT_PARAMETER, "unParsable")
        )

        assertEquals(INCORRECT_HEIGHT, t.processRequest(request))
    }

    @Test
    fun processRequest_withHeight_incorrectHeight_isNegative() {
        val heightValue = -1L

        val request = QuickMocker.httpServletRequest(
                MockParam(HEIGHT_PARAMETER, heightValue)
        )

        assertEquals(INCORRECT_HEIGHT, t.processRequest(request))
    }

    @Test
    fun processRequest_withHeight_incorrectHeight_overCurrentBlockHeight() {
        val heightValue = 10L

        val request = QuickMocker.httpServletRequest(
                MockParam(HEIGHT_PARAMETER, heightValue)
        )

        whenever(blockchainMock.height).doReturn(5)

        assertEquals(INCORRECT_HEIGHT, t.processRequest(request))
    }

    @Test
    fun processRequest_withTimestamp() {
        val timestamp = 2

        val request = QuickMocker.httpServletRequest(
                MockParam(TIMESTAMP_PARAMETER, timestamp)
        )

        val mockBlock = mock<Block>()

        whenever(blockchainMock.getLastBlock(eq(timestamp))).doReturn(mockBlock)

        val result = t.processRequest(request) as JsonObject

        assertNotNull(result)
    }

    @Test
    fun processRequest_withTimestamp_incorrectTimeStamp_unParsable() {
        val request = QuickMocker.httpServletRequest(
                MockParam(TIMESTAMP_PARAMETER, "unParsable")
        )

        assertEquals(INCORRECT_TIMESTAMP, t.processRequest(request))
    }

    @Test
    fun processRequest_withTimestamp_incorrectTimeStamp_negative() {
        val timestamp = -1

        val request = QuickMocker.httpServletRequest(
                MockParam(TIMESTAMP_PARAMETER, timestamp)
        )

        assertEquals(INCORRECT_TIMESTAMP, t.processRequest(request))
    }


    @Test
    fun processRequest_unknownBlock() {
        val request = QuickMocker.httpServletRequest()

        assertEquals(UNKNOWN_BLOCK, t.processRequest(request))
    }

}
