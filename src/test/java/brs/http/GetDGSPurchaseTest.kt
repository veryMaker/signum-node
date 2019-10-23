package brs.http

import brs.DigitalGoodsStore.Purchase
import brs.common.QuickMocker
import brs.crypto.EncryptedData
import brs.http.common.ResultFields.BUYER_RESPONSE
import brs.http.common.ResultFields.DELIVERY_DEADLINE_TIMESTAMP_RESPONSE
import brs.http.common.ResultFields.DISCOUNT_NQT_RESPONSE
import brs.http.common.ResultFields.GOODS_RESPONSE
import brs.http.common.ResultFields.NAME_RESPONSE
import brs.http.common.ResultFields.PENDING_RESPONSE
import brs.http.common.ResultFields.PRICE_NQT_RESPONSE
import brs.http.common.ResultFields.PURCHASE_RESPONSE
import brs.http.common.ResultFields.QUANTITY_RESPONSE
import brs.http.common.ResultFields.REFUND_NQT_RESPONSE
import brs.http.common.ResultFields.SELLER_RESPONSE
import brs.http.common.ResultFields.TIMESTAMP_RESPONSE
import brs.services.ParameterService
import brs.util.safeGetAsBoolean
import brs.util.safeGetAsLong
import brs.util.safeGetAsString
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

class GetDGSPurchaseTest {

    private lateinit var t: GetDGSPurchase

    private lateinit var mockParameterService: ParameterService

    @Before
    fun setUp() {
        mockParameterService = mock()

        t = GetDGSPurchase(mockParameterService)
    }


    @Test
    fun processRequest() = runBlocking {
        val request = QuickMocker.httpServletRequest()

        val mockEncryptedData = mock<EncryptedData>()

        whenever(mockEncryptedData.data).doReturn(byteArrayOf(1.toByte()))
        whenever(mockEncryptedData.nonce).doReturn(byteArrayOf(1.toByte()))

        val mockEncryptedDataList = mutableListOf(mockEncryptedData)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.id).doReturn(1L)
        whenever(mockPurchase.goodsId).doReturn(2L)
        whenever(mockPurchase.name).doReturn("name")
        whenever(mockPurchase.sellerId).doReturn(3L)
        whenever(mockPurchase.priceNQT).doReturn(4L)
        whenever(mockPurchase.quantity).doReturn(5)
        whenever(mockPurchase.buyerId).doReturn(6L)
        whenever(mockPurchase.timestamp).doReturn(7)
        whenever(mockPurchase.deliveryDeadlineTimestamp).doReturn(8)
        whenever(mockPurchase.isPending).doReturn(true)
        whenever(mockPurchase.goodsIsText()).doReturn(true)
        whenever(mockPurchase.discountNQT).doReturn(8L)
        whenever(mockPurchase.refundNQT).doReturn(9L)
        whenever(mockPurchase.encryptedGoods).doReturn(mockEncryptedData)
        whenever(mockPurchase.feedbackNotes).doReturn(mockEncryptedDataList)
        whenever(mockPurchase.refundNote).doReturn(mockEncryptedData)
        whenever(mockPurchase.note).doReturn(mockEncryptedData)
        whenever(mockPurchase.publicFeedback).doReturn(listOf("feedback"))

        whenever(mockParameterService.getPurchase(eq(request))).doReturn(mockPurchase)

        val result = t.processRequest(request) as JsonObject

        assertNotNull(result)

        assertEquals("" + mockPurchase.id, result.get(PURCHASE_RESPONSE).safeGetAsString())
        assertEquals("" + mockPurchase.goodsId, result.get(GOODS_RESPONSE).safeGetAsString())
        assertEquals(mockPurchase.name, result.get(NAME_RESPONSE).safeGetAsString())
        assertEquals("" + mockPurchase.sellerId, result.get(SELLER_RESPONSE).safeGetAsString())
        assertEquals("" + mockPurchase.priceNQT, result.get(PRICE_NQT_RESPONSE).safeGetAsString())
        assertEquals(mockPurchase.quantity.toLong(), result.get(QUANTITY_RESPONSE).safeGetAsLong())
        assertEquals("" + mockPurchase.buyerId, result.get(BUYER_RESPONSE).safeGetAsString())
        assertEquals(mockPurchase.timestamp.toLong(), result.get(TIMESTAMP_RESPONSE).safeGetAsLong())
        assertEquals(mockPurchase.deliveryDeadlineTimestamp.toLong(), result.get(DELIVERY_DEADLINE_TIMESTAMP_RESPONSE).safeGetAsLong())
        assertEquals(mockPurchase.isPending, result.get(PENDING_RESPONSE).safeGetAsBoolean())
        assertEquals("" + mockPurchase.discountNQT, result.get(DISCOUNT_NQT_RESPONSE).safeGetAsString())
        assertEquals("" + mockPurchase.refundNQT, result.get(REFUND_NQT_RESPONSE).safeGetAsString())
    }

}
