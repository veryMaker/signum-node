package brs.http

import brs.Account
import brs.Block
import brs.Blockchain
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.http.common.ResultFields.BLOCKS_RESPONSE
import brs.services.BlockService
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAccountBlocksTest : AbstractUnitTest() {

    private lateinit var t: GetAccountBlocks

    private lateinit var blockchainMock: Blockchain
    private lateinit var parameterServiceMock: ParameterService
    private lateinit var blockServiceMock: BlockService

    @Before
    fun setUp() {
        blockchainMock = mock<Blockchain>()
        parameterServiceMock = mock<ParameterService>()
        blockServiceMock = mock<BlockService>()

        t = GetAccountBlocks(blockchainMock!!, parameterServiceMock!!, blockServiceMock!!)
    }

    @Test
    fun processRequest() = runBlocking {
        val mockTimestamp = 1
        val mockFirstIndex = 2
        val mockLastIndex = 3

        val request = QuickMocker.httpServletRequest(
                MockParam(FIRST_INDEX_PARAMETER, "" + mockFirstIndex),
                MockParam(LAST_INDEX_PARAMETER, "" + mockLastIndex),
                MockParam(TIMESTAMP_PARAMETER, "" + mockTimestamp)
        )

        val mockAccount = mock<Account>()
        val mockBlock = mock<Block>()


        whenever(parameterServiceMock!!.getAccount(request)).doReturn(mockAccount)

        val mockBlockIterator = mockCollection<Block>(mockBlock)
        whenever(blockchainMock!!.getBlocks(eq(mockAccount), eq(mockTimestamp), eq(mockFirstIndex), eq(mockLastIndex))).doReturn(mockBlockIterator)

        val result = t!!.processRequest(request) as JsonObject

        val blocks = result.get(BLOCKS_RESPONSE) as JsonArray
        assertNotNull(blocks)
        assertEquals(1, blocks.size().toLong())

        val resultBlock = blocks.get(0) as JsonObject
        assertNotNull(resultBlock)

        //TODO validate all fields
    }
}