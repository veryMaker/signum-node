package brs.http

import brs.Account
import brs.Block
import brs.Blockchain
import brs.BurstException
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.services.ParameterService
import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest
import brs.http.common.ResultFields.BLOCK_IDS_RESPONSE
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetAccountBlockIdsTest : AbstractUnitTest() {

    private var t: GetAccountBlockIds? = null

    private var mockParameterService: ParameterService? = null
    private var mockBlockchain: Blockchain? = null

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()
        mockBlockchain = mock<Blockchain>()

        t = GetAccountBlockIds(mockParameterService!!, mockBlockchain!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
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
        val mockBlocksIterator = mockCollection<Block>(mockBlock)

        whenever(mockParameterService!!.getAccount(request)).doReturn(mockAccount)
        whenever(mockBlockchain!!.getBlocks(eq(mockAccount), eq(timestamp), eq(firstIndex), eq(lastIndex)))
                .doReturn(mockBlocksIterator)

        val result = t!!.processRequest(request) as JsonObject
        assertNotNull(result)

        val blockIds = result.get(BLOCK_IDS_RESPONSE) as JsonArray
        assertNotNull(blockIds)
        assertEquals(1, blockIds.size().toLong())
        assertEquals(mockBlockStringId, JSON.getAsString(blockIds.get(0)))
    }
}