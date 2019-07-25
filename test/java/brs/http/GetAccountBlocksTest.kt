package brs.http

import brs.Account
import brs.Block
import brs.Blockchain
import brs.BurstException
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.services.BlockService
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import brs.http.common.Parameters.*
import brs.http.common.ResultFields.BLOCKS_RESPONSE
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAccountBlocksTest : AbstractUnitTest() {

    private var t: GetAccountBlocks? = null

    private var blockchainMock: Blockchain? = null
    private var parameterServiceMock: ParameterService? = null
    private var blockServiceMock: BlockService? = null

    @Before
    fun setUp() {
        blockchainMock = mock<Blockchain>()
        parameterServiceMock = mock<ParameterService>()
        blockServiceMock = mock<BlockService>()

        t = GetAccountBlocks(blockchainMock!!, parameterServiceMock!!, blockServiceMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val mockTimestamp = 1
        val mockFirstIndex = 2
        val mockLastIndex = 3

        val req = QuickMocker.httpServletRequest(
                MockParam(FIRST_INDEX_PARAMETER, "" + mockFirstIndex),
                MockParam(LAST_INDEX_PARAMETER, "" + mockLastIndex),
                MockParam(TIMESTAMP_PARAMETER, "" + mockTimestamp)
        )

        val mockAccount = mock<Account>()
        val mockBlock = mock<Block>()


        whenever(parameterServiceMock!!.getAccount(req)).thenReturn(mockAccount)

        val mockBlockIterator = mockCollection<Block>(mockBlock)
        whenever(blockchainMock!!.getBlocks(eq(mockAccount), eq(mockTimestamp), eq(mockFirstIndex), eq(mockLastIndex))).thenReturn(mockBlockIterator)

        val result = t!!.processRequest(req) as JsonObject

        val blocks = result.get(BLOCKS_RESPONSE) as JsonArray
        assertNotNull(blocks)
        assertEquals(1, blocks.size().toLong())

        val resultBlock = blocks.get(0) as JsonObject
        assertNotNull(resultBlock)

        //TODO validate all fields
    }
}