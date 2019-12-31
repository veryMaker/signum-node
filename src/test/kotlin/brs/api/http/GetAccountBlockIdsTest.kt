package brs.api.http

import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.api.http.common.ResultFields.BLOCK_IDS_RESPONSE
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.entity.Account
import brs.entity.Block
import brs.services.BlockchainService
import brs.services.ParameterService
import brs.util.json.safeGetAsString
import com.google.gson.JsonArray
import brs.util.jetty.get
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetAccountBlockIdsTest : AbstractUnitTest() {

    private lateinit var t: GetAccountBlockIds

    private lateinit var mockParameterService: ParameterService
    private lateinit var mockBlockchainService: BlockchainService

    @Before
    fun setUp() {
        mockParameterService = mockk(relaxed = true)
        mockBlockchainService = mockk(relaxed = true)

        t = GetAccountBlockIds(mockParameterService, mockBlockchainService)
    }

    @Test
    fun processRequest() {
        val timestamp = 1
        val firstIndex = 0
        val lastIndex = 1

        val request = QuickMocker.httpServletRequest(
                MockParam(TIMESTAMP_PARAMETER, timestamp),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex)
        )

        val mockAccount = mockk<Account>(relaxed = true)

        val mockBlockStringId = "mockBlockStringId"
        val mockBlock = mockk<Block>(relaxed = true)
        every { mockBlock.stringId } returns mockBlockStringId
        val mockBlocksIterator = mockCollection(mockBlock)

        every { mockParameterService.getAccount(request) } returns mockAccount
        every { mockBlockchainService.getBlocks(eq(mockAccount), eq(timestamp), eq(firstIndex), eq(lastIndex)) } returns mockBlocksIterator

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val blockIds = result.get(BLOCK_IDS_RESPONSE) as JsonArray
        assertNotNull(blockIds)
        assertEquals(1, blockIds.size().toLong())
        assertEquals(mockBlockStringId, blockIds.get(0).safeGetAsString())
    }
}