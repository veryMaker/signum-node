package brs.api.http

import brs.entity.DigitalGoodsStore.Purchase
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
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetDGSPurchasesTest : AbstractUnitTest() {

    private lateinit var t: GetDGSPurchases

    private lateinit var mockDigitalGoodsStoreService: DigitalGoodsStoreService

    @Before
    fun setUp() {
        mockDigitalGoodsStoreService = mock()

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

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.isPending).doReturn(false)

        val mockGoodsIterator = mockCollection(mockPurchase)

        whenever(mockDigitalGoodsStoreService.getAllPurchases(eq(0), eq(-1))).doReturn(mockGoodsIterator)

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

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.isPending).doReturn(false)

        val mockGoodsIterator = mockCollection(mockPurchase)

        whenever(mockDigitalGoodsStoreService.getSellerPurchases(eq(1L), eq(0), eq(-1))).doReturn(mockGoodsIterator)

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

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.isPending).doReturn(false)

        val mockGoodsIterator = mockCollection(mockPurchase)

        whenever(mockDigitalGoodsStoreService.getBuyerPurchases(eq(1L), eq(0), eq(-1))).doReturn(mockGoodsIterator)

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

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.isPending).doReturn(false)

        val mockGoodsIterator = mockCollection(mockPurchase)

        whenever(mockDigitalGoodsStoreService.getSellerBuyerPurchases(eq(1L), eq(2L), eq(0), eq(-1))).doReturn(mockGoodsIterator)

        val result = t.processRequest(request) as JsonObject
        assertNotNull(result)

        val purchasesResult = result.get(PURCHASES_RESPONSE) as JsonArray
        assertNotNull(purchasesResult)
        assertEquals(1, purchasesResult.size().toLong())
    }

}
