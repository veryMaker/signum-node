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
import brs.http.common.ResultFields.BLOCK_IDS_RESPONSE
import brs.services.ParameterService
import brs.util.safeGetAsString
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

class GetAccountBlockIdsTest : AbstractUnitTest() {

    private lateinit var t: GetAccountBlockIds

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockBlockchain: Blockchain

    @Before
    fun setUp() {
        mockParameterService = mock()
        mockBlockchain = mock()

        t = GetAccountBlockIds(mockParameterService, mockBlockchain)
    }

    @Test
    fun processRequest() = runBlocking {
        val timestamp = 1
        val firstIndex = 0
        val lastIndex = 1

        val request = QuickMocker.httpServletRequest(
                MockParam(TIMESTAMP_PARAMETER, timestamp),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex)
        )

        val mockAccount = mock<Account>()

        val mockBlockStringId = "mockBlockStringId"
        val mockBlock = mock<Block>()
        whenever(mockBlock.stringId).doReturn(mockBlockStringId)
        val mockBlocksIterator = mockCollection(mockBlock)

        whenever(mockParameterService.getAccount(request)).doReturn(mockAccount)
        whenever(mockBlockchain.getBlocks(eq(mockAccount), eq(timestamp), eq(firstIndex), eq(lastIndex)))
                .doReturn(mockBlocksIterator)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val blockIds = result.get(BLOCK_IDS_RESPONSE) as JsonArray
        assertNotNull(blockIds)
        assertEquals(1, blockIds.size().toLong())
        assertEquals(mockBlockStringId, blockIds.get(0).safeGetAsString())
    }
}