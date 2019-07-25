package brs.http

import brs.BurstException
import brs.DigitalGoodsStore.Purchase
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.services.DGSGoodsStoreService
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.*
import brs.http.common.ResultFields.PURCHASES_RESPONSE
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetDGSPurchasesTest : AbstractUnitTest() {

    private var t: GetDGSPurchases? = null

    private var mockDGSGoodsStoreService: DGSGoodsStoreService? = null

    @Before
    fun setUp() {
        mockDGSGoodsStoreService = mock<DGSGoodsStoreService>()

        t = GetDGSPurchases(mockDGSGoodsStoreService!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_getAllPurchases() {
        val req = QuickMocker.httpServletRequest(
                MockParam(SELLER_PARAMETER, 0L),
                MockParam(BUYER_PARAMETER, 0L),
                MockParam(FIRST_INDEX_PARAMETER, 0L),
                MockParam(LAST_INDEX_PARAMETER, -1L),
                MockParam(COMPLETED_PARAMETER, false)
        )

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.isPending).thenReturn(false)

        val mockGoodsIterator = mockCollection<Purchase>(mockPurchase)

        whenever(mockDGSGoodsStoreService!!.getAllPurchases(eq(0), eq(-1))).thenReturn(mockGoodsIterator)

        val result = t!!.processRequest(req) as JsonObject
        assertNotNull(result)

        val purchasesResult = result.get(PURCHASES_RESPONSE) as JsonArray
        assertNotNull(purchasesResult)
        assertEquals(1, purchasesResult.size().toLong())
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_getSellerPurchases() {
        val req = QuickMocker.httpServletRequest(
                MockParam(SELLER_PARAMETER, 1L),
                MockParam(BUYER_PARAMETER, 0L),
                MockParam(FIRST_INDEX_PARAMETER, 0L),
                MockParam(LAST_INDEX_PARAMETER, -1L),
                MockParam(COMPLETED_PARAMETER, false)
        )

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.isPending).thenReturn(false)

        val mockGoodsIterator = mockCollection<Purchase>(mockPurchase)

        whenever(mockDGSGoodsStoreService!!.getSellerPurchases(eq(1L), eq(0), eq(-1))).thenReturn(mockGoodsIterator)

        val result = t!!.processRequest(req) as JsonObject
        assertNotNull(result)

        val purchasesResult = result.get(PURCHASES_RESPONSE) as JsonArray
        assertNotNull(purchasesResult)
        assertEquals(1, purchasesResult.size().toLong())
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_getBuyerPurchases() {
        val req = QuickMocker.httpServletRequest(
                MockParam(SELLER_PARAMETER, 0L),
                MockParam(BUYER_PARAMETER, 1L),
                MockParam(FIRST_INDEX_PARAMETER, 0L),
                MockParam(LAST_INDEX_PARAMETER, -1L),
                MockParam(COMPLETED_PARAMETER, false)
        )

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.isPending).thenReturn(false)

        val mockGoodsIterator = mockCollection<Purchase>(mockPurchase)

        whenever(mockDGSGoodsStoreService!!.getBuyerPurchases(eq(1L), eq(0), eq(-1))).thenReturn(mockGoodsIterator)

        val result = t!!.processRequest(req) as JsonObject
        assertNotNull(result)

        val purchasesResult = result.get(PURCHASES_RESPONSE) as JsonArray
        assertNotNull(purchasesResult)
        assertEquals(1, purchasesResult.size().toLong())
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest_getSellerBuyerPurchases() {
        val req = QuickMocker.httpServletRequest(
                MockParam(SELLER_PARAMETER, 1L),
                MockParam(BUYER_PARAMETER, 2L),
                MockParam(FIRST_INDEX_PARAMETER, 0L),
                MockParam(LAST_INDEX_PARAMETER, -1L),
                MockParam(COMPLETED_PARAMETER, false)
        )

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.isPending).thenReturn(false)

        val mockGoodsIterator = mockCollection<Purchase>(mockPurchase)

        whenever(mockDGSGoodsStoreService!!.getSellerBuyerPurchases(eq(1L), eq(2L), eq(0), eq(-1))).thenReturn(mockGoodsIterator)

        val result = t!!.processRequest(req) as JsonObject
        assertNotNull(result)

        val purchasesResult = result.get(PURCHASES_RESPONSE) as JsonArray
        assertNotNull(purchasesResult)
        assertEquals(1, purchasesResult.size().toLong())
    }

}
