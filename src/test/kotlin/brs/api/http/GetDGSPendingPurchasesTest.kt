package brs.api.http

import brs.entity.Purchase
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.api.http.common.JSONResponses.MISSING_SELLER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.Parameters.SELLER_PARAMETER
import brs.api.http.common.ResultFields.PURCHASES_RESPONSE
import brs.services.DigitalGoodsStoreService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetDGSPendingPurchasesTest : AbstractUnitTest() {
    private lateinit var t: GetDGSPendingPurchases

    private lateinit var mockDigitalGoodStoreService: DigitalGoodsStoreService

    @Before
    fun setUp() {
        mockDigitalGoodStoreService = mockk(relaxed = true)

        t = GetDGSPendingPurchases(mockDigitalGoodStoreService)
    }

    @Test
    fun processRequest() {
        val sellerId = 123L
        val firstIndex = 1
        val lastIndex = 2

        val request = QuickMocker.httpServletRequest(
                MockParam(SELLER_PARAMETER, sellerId),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex),
                MockParam(LAST_INDEX_PARAMETER, lastIndex)
        )

        val mockPurchase = mockk<Purchase>(relaxed = true)

        val mockPurchaseIterator = mockCollection(mockPurchase)
        every { mockDigitalGoodStoreService.getPendingSellerPurchases(eq(sellerId), eq(firstIndex), eq(lastIndex)) } returns mockPurchaseIterator

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val resultPurchases = result.get(PURCHASES_RESPONSE) as JsonArray

        assertNotNull(resultPurchases)
        assertEquals(1, resultPurchases.size().toLong())
    }

    @Test
    fun processRequest_missingSeller() {
        val request = QuickMocker.httpServletRequest(
                MockParam(SELLER_PARAMETER, 0L)
        )

        assertEquals(MISSING_SELLER, t.processRequest(request))
    }
}
