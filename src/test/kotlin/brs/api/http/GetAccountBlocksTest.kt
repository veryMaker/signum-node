package brs.api.http

import brs.entity.Account
import brs.entity.Block
import brs.services.BlockchainService
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.api.http.common.ResultFields.BLOCKS_RESPONSE
import brs.services.BlockService
import brs.services.ParameterService
import com.google.gson.JsonArray
import brs.util.jetty.get
import com.google.gson.JsonObject
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAccountBlocksTest : AbstractUnitTest() {
    private lateinit var t: GetAccountBlocks

    private lateinit var blockchainServiceMock: BlockchainService
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var blockServiceMock: BlockService

    @Before
    fun setUp() {
        blockchainServiceMock = mockk(relaxed = true)
        parameterServiceMock = mockk(relaxed = true)
        blockServiceMock = mockk(relaxed = true)

        t = GetAccountBlocks(blockchainServiceMock, parameterServiceMock, blockServiceMock)
    }

    @Test
    fun processRequest() {
        val mockTimestamp = 1
        val mockFirstIndex = 2
        val mockLastIndex = 3

        val request = QuickMocker.httpServletRequest(
                MockParam(FIRST_INDEX_PARAMETER, mockFirstIndex.toString()),
                MockParam(LAST_INDEX_PARAMETER, mockLastIndex.toString()),
                MockParam(TIMESTAMP_PARAMETER, mockTimestamp.toString())
        )

        val mockAccount = mockk<Account>(relaxed = true)
        val mockBlock = mockk<Block>(relaxed = true)


        every { parameterServiceMock.getAccount(request) } returns mockAccount

        val mockBlockIterator = mockCollection(mockBlock)
        every { blockchainServiceMock.getBlocks(eq(mockAccount), eq(mockTimestamp), eq(mockFirstIndex), eq(mockLastIndex)) } returns mockBlockIterator

        val result = t.processRequest(request) as JsonObject

        val blocks = result.get(BLOCKS_RESPONSE) as JsonArray
        assertNotNull(blocks)
        assertEquals(1, blocks.size().toLong())

        val resultBlock = blocks.get(0) as JsonObject
        assertNotNull(resultBlock)

        //TODO validate all fields
    }
}