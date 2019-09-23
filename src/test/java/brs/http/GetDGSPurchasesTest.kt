package brs.http

import brs.DigitalGoodsStore.Purchase
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.http.common.Parameters.BUYER_PARAMETER
import brs.http.common.Parameters.COMPLETED_PARAMETER
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.Parameters.SELLER_PARAMETER
import brs.http.common.ResultFields.PURCHASES_RESPONSE
import brs.services.DGSGoodsStoreService
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

    private var t: GetDGSPurchases? = null

    private var mockDGSGoodsStoreService: DGSGoodsStoreService? = null

    @Before
    fun setUp() {
        mockDGSGoodsStoreService = mock<DGSGoodsStoreService>()

        t = GetDGSPurchases(mockDGSGoodsStoreService!!)
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

        val mockGoodsIterator = mockCollection<Purchase>(mockPurchase)

        whenever(mockDGSGoodsStoreService!!.getAllPurchases(eq(0), eq(-1))).doReturn(mockGoodsIterator)

        val result = t!!.processRequest(request) as JsonObject
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

        val mockGoodsIterator = mockCollection<Purchase>(mockPurchase)

        whenever(mockDGSGoodsStoreService!!.getSellerPurchases(eq(1L), eq(0), eq(-1))).doReturn(mockGoodsIterator)

        val result = t!!.processRequest(request) as JsonObject
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

        val mockGoodsIterator = mockCollection<Purchase>(mockPurchase)

        whenever(mockDGSGoodsStoreService!!.getBuyerPurchases(eq(1L), eq(0), eq(-1))).doReturn(mockGoodsIterator)

        val result = t!!.processRequest(request) as JsonObject
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

        val mockGoodsIterator = mockCollection<Purchase>(mockPurchase)

        whenever(mockDGSGoodsStoreService!!.getSellerBuyerPurchases(eq(1L), eq(2L), eq(0), eq(-1))).doReturn(mockGoodsIterator)

        val result = t!!.processRequest(request) as JsonObject
        assertNotNull(result)

        val purchasesResult = result.get(PURCHASES_RESPONSE) as JsonArray
        assertNotNull(purchasesResult)
        assertEquals(1, purchasesResult.size().toLong())
    }

}
