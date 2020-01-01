package brs.api.http

import brs.entity.Purchase
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.api.http.common.Parameters.BUYER_PARAMETER
import brs.api.http.common.Parameters.COMPLETED_PARAMETER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.Parameters.SELLER_PARAMETER
import brs.api.http.common.ResultFields.PURCHASES_RESPONSE
import brs.services.DigitalGoodsStoreService
import com.google.gson.JsonArray
import brs.util.jetty.get
import com.google.gson.JsonObject
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetDGSPurchasesTest : AbstractUnitTest() {
    private lateinit var t: GetDGSPurchases

    private lateinit var mockDigitalGoodsStoreService: DigitalGoodsStoreService

    @Before
    fun setUp() {
        mockDigitalGoodsStoreService = mockk(relaxed = true)

        t = GetDGSPurchases(mockDigitalGoodsStoreService)
    }

    @Test
    fun processRequest_getAllPurchases() {
        val request = QuickMocker.httpServletRequest(
                MockParam(SELLER_PARAMETER, 0L),
                MockParam(BUYER_PARAMETER, 0L),
                MockParam(FIRST_INDEX_PARAMETER, 0L),
                MockParam(LAST_INDEX_PARAMETER, -1L),
                MockParam(COMPLETED_PARAMETER, false)
        )

        val mockPurchase = mockk<Purchase>(relaxed = true)
        every { mockPurchase.isPending } returns false

        val mockGoodsIterator = mockCollection(mockPurchase)

        every { mockDigitalGoodsStoreService.getAllPurchases(eq(0), eq(-1)) } returns mockGoodsIterator

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val purchasesResult = result.get(PURCHASES_RESPONSE) as JsonArray
        assertNotNull(purchasesResult)
        assertEquals(1, purchasesResult.size().toLong())
    }

    @Test
    fun processRequest_getSellerPurchases() {
        val request = QuickMocker.httpServletRequest(
                MockParam(SELLER_PARAMETER, 1L),
                MockParam(BUYER_PARAMETER, 0L),
                MockParam(FIRST_INDEX_PARAMETER, 0L),
                MockParam(LAST_INDEX_PARAMETER, -1L),
                MockParam(COMPLETED_PARAMETER, false)
        )

        val mockPurchase = mockk<Purchase>(relaxed = true)
        every { mockPurchase.isPending } returns false

        val mockGoodsIterator = mockCollection(mockPurchase)

        every { mockDigitalGoodsStoreService.getSellerPurchases(eq(1L), eq(0), eq(-1)) } returns mockGoodsIterator

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val purchasesResult = result.get(PURCHASES_RESPONSE) as JsonArray
        assertNotNull(purchasesResult)
        assertEquals(1, purchasesResult.size().toLong())
    }

    @Test
    fun processRequest_getBuyerPurchases() {
        val request = QuickMocker.httpServletRequest(
                MockParam(SELLER_PARAMETER, 0L),
                MockParam(BUYER_PARAMETER, 1L),
                MockParam(FIRST_INDEX_PARAMETER, 0L),
                MockParam(LAST_INDEX_PARAMETER, -1L),
                MockParam(COMPLETED_PARAMETER, false)
        )

        val mockPurchase = mockk<Purchase>(relaxed = true)
        every { mockPurchase.isPending } returns false

        val mockGoodsIterator = mockCollection(mockPurchase)

        every { mockDigitalGoodsStoreService.getBuyerPurchases(eq(1L), eq(0), eq(-1)) } returns mockGoodsIterator

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val purchasesResult = result.get(PURCHASES_RESPONSE) as JsonArray
        assertNotNull(purchasesResult)
        assertEquals(1, purchasesResult.size().toLong())
    }

    @Test
    fun processRequest_getSellerBuyerPurchases() {
        val request = QuickMocker.httpServletRequest(
                MockParam(SELLER_PARAMETER, 1L),
                MockParam(BUYER_PARAMETER, 2L),
                MockParam(FIRST_INDEX_PARAMETER, 0L),
                MockParam(LAST_INDEX_PARAMETER, -1L),
                MockParam(COMPLETED_PARAMETER, false)
        )

        val mockPurchase = mockk<Purchase>(relaxed = true)
        every { mockPurchase.isPending } returns false

        val mockGoodsIterator = mockCollection(mockPurchase)

        every { mockDigitalGoodsStoreService.getSellerBuyerPurchases(eq(1L), eq(2L), eq(0), eq(-1)) } returns mockGoodsIterator

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val purchasesResult = result.get(PURCHASES_RESPONSE) as JsonArray
        assertNotNull(purchasesResult)
        assertEquals(1, purchasesResult.size().toLong())
    }
}
