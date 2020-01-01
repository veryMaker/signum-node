package brs.api.http

import brs.api.http.common.JSONResponses.INCORRECT_BLOCK
import brs.api.http.common.JSONResponses.INCORRECT_HEIGHT
import brs.api.http.common.JSONResponses.INCORRECT_TIMESTAMP
import brs.api.http.common.JSONResponses.UNKNOWN_BLOCK
import brs.api.http.common.Parameters.BLOCK_PARAMETER
import brs.api.http.common.Parameters.HEIGHT_PARAMETER
import brs.api.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.entity.Block
import brs.services.BlockService
import brs.services.BlockchainService
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetBlockTest {
    private lateinit var t: GetBlock

    private lateinit var blockchainServiceMock: BlockchainService
    private lateinit var blockServiceMock: BlockService

    @Before
    fun setUp() {
        blockchainServiceMock = mockk()
        every { blockchainServiceMock.getBlock(any()) } returns null
        every { blockchainServiceMock.height } returns 0
        every { blockchainServiceMock.lastBlock } returns mockk(relaxed = true)
        blockServiceMock = mockk(relaxed = true)

        t = GetBlock(blockchainServiceMock, blockServiceMock)
    }

    @Test
    fun processRequest_withBlockId() {
        val blockId = 2L

        val request = QuickMocker.httpServletRequest(
                MockParam(BLOCK_PARAMETER, blockId)
        )

        val mockBlock = mockk<Block>(relaxed = true)

        every { blockchainServiceMock.getBlock(eq(blockId)) } returns mockBlock

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

        val mockBlock = mockk<Block>(relaxed = true)

        every { blockchainServiceMock.height } returns 100
        every { blockchainServiceMock.getBlockAtHeight(eq(blockHeight)) } returns mockBlock

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

        every { blockchainServiceMock.height } returns 5

        assertEquals(INCORRECT_HEIGHT, t.processRequest(request))
    }

    @Test
    fun processRequest_withTimestamp() {
        val timestamp = 2

        val request = QuickMocker.httpServletRequest(
                MockParam(TIMESTAMP_PARAMETER, timestamp)
        )

        val mockBlock = mockk<Block>(relaxed = true)

        every { blockchainServiceMock.getLastBlock(eq(timestamp)) } returns mockBlock

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
        val request = QuickMocker.httpServletRequest(MockParam(BLOCK_PARAMETER, 123L))

        assertEquals(UNKNOWN_BLOCK, t.processRequest(request))
    }
}
