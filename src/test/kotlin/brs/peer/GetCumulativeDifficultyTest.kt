package brs.peer

import brs.Block
import brs.Blockchain
import brs.common.QuickMocker
import brs.util.JSON
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

    private lateinit var mockBlockchain: Blockchain

    @Before
    fun setUp() {
        mockBlockchain = mock()

        t = GetCumulativeDifficulty(mockBlockchain)
    }

    @Test
    fun processRequest() {
        val cumulativeDifficulty = BigInteger.TEN
        val blockchainHeight = 50

        val request = QuickMocker.jsonObject()

        val mockLastBlock = mock<Block>()
        whenever(mockLastBlock.height).doReturn(blockchainHeight)
        whenever(mockLastBlock.cumulativeDifficulty).doReturn(cumulativeDifficulty)

        whenever(mockBlockchain.lastBlock).doReturn(mockLastBlock)

        val result = t.processRequest(request, mock()) as JsonObject
        assertNotNull(result)

        assertEquals(cumulativeDifficulty.toString(), result.get("cumulativeDifficulty").safeGetAsString())
        assertEquals(blockchainHeight.toLong(), result.get("blockchainHeight").safeGetAsLong())
    }

}
