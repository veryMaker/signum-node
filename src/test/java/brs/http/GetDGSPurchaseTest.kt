package brs.http

import brs.BurstException
import brs.DigitalGoodsStore.Purchase
import brs.common.QuickMocker
import brs.crypto.EncryptedData
import brs.services.ParameterService
import brs.util.JSON
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest
import java.util.Arrays

import brs.http.common.ResultFields.*
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class GetDGSPurchaseTest {

    private var t: GetDGSPurchase? = null

    private var mockParameterService: ParameterService? = null

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()

        t = GetDGSPurchase(mockParameterService!!)
    }


    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val req = QuickMocker.httpServletRequest()

        val mockEncryptedData = mock<EncryptedData>()

        whenever(mockEncryptedData.data).doReturn(byteArrayOf(1.toByte()))
        whenever(mockEncryptedData.nonce).doReturn(byteArrayOf(1.toByte()))

        val mockEncryptedDataList = Arrays.asList(mockEncryptedData)

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

        whenever(mockParameterService!!.getPurchase(eq<HttpServletRequest>(req))).doReturn(mockPurchase)

        val result = t!!.processRequest(req) as JsonObject

        assertNotNull(result)

        assertEquals("" + mockPurchase.id, JSON.getAsString(result.get(PURCHASE_RESPONSE)))
        assertEquals("" + mockPurchase.goodsId, JSON.getAsString(result.get(GOODS_RESPONSE)))
        assertEquals(mockPurchase.name, JSON.getAsString(result.get(NAME_RESPONSE)))
        assertEquals("" + mockPurchase.sellerId, JSON.getAsString(result.get(SELLER_RESPONSE)))
        assertEquals("" + mockPurchase.priceNQT, JSON.getAsString(result.get(PRICE_NQT_RESPONSE)))
        assertEquals(mockPurchase.quantity.toLong(), JSON.getAsInt(result.get(QUANTITY_RESPONSE)).toLong())
        assertEquals("" + mockPurchase.buyerId, JSON.getAsString(result.get(BUYER_RESPONSE)))
        assertEquals(mockPurchase.timestamp.toLong(), JSON.getAsInt(result.get(TIMESTAMP_RESPONSE)).toLong())
        assertEquals(mockPurchase.deliveryDeadlineTimestamp.toLong(), JSON.getAsInt(result.get(DELIVERY_DEADLINE_TIMESTAMP_RESPONSE)).toLong())
        assertEquals(mockPurchase.isPending, JSON.getAsBoolean(result.get(PENDING_RESPONSE)))
        assertEquals("" + mockPurchase.discountNQT, JSON.getAsString(result.get(DISCOUNT_NQT_RESPONSE)))
        assertEquals("" + mockPurchase.refundNQT, JSON.getAsString(result.get(REFUND_NQT_RESPONSE)))
    }

}
