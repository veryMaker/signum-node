package brs.peer

import brs.entity.Block
import brs.services.BlockchainService
import brs.common.QuickMocker
import brs.util.safeGetAsLong
import brs.util.safeGetAsString
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.math.BigInteger

class GetCumulativeDifficultyTest {

    private lateinit var t: GetCumulativeDifficulty

    private lateinit var mockBlockchainService: BlockchainService

    @Before
    fun setUp() {
        mockBlockchainService = mock()

        t = GetCumulativeDifficulty(mockBlockchainService)
    }

    @Test
    fun processRequest() {
        val cumulativeDifficulty = BigInteger.TEN
        val blockchainHeight = 50

        val request = QuickMocker.jsonObject()

        val mockLastBlock = mock<Block>()
        whenever(mockLastBlock.height).doReturn(blockchainHeight)
        whenever(mockLastBlock.cumulativeDifficulty).doReturn(cumulativeDifficulty)

        whenever(mockBlockchainService.lastBlock).doReturn(mockLastBlock)

        val result = t.processRequest(request, mock()) as JsonObject
        assertNotNull(result)

        assertEquals(cumulativeDifficulty.toString(), result.get("cumulativeDifficulty").safeGetAsString())
        assertEquals(blockchainHeight.toLong(), result.get("blockchainHeight").safeGetAsLong())
    }

}
