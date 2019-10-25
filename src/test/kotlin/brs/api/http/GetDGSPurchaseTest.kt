package brs.api.http

import brs.entity.Purchase
import brs.common.QuickMocker
import brs.entity.EncryptedData
import brs.api.http.common.ResultFields.BUYER_RESPONSE
import brs.api.http.common.ResultFields.DELIVERY_DEADLINE_TIMESTAMP_RESPONSE
import brs.api.http.common.ResultFields.DISCOUNT_PLANCK_RESPONSE
import brs.api.http.common.ResultFields.GOODS_RESPONSE
import brs.api.http.common.ResultFields.NAME_RESPONSE
import brs.api.http.common.ResultFields.PENDING_RESPONSE
import brs.api.http.common.ResultFields.PRICE_PLANCK_RESPONSE
import brs.api.http.common.ResultFields.PURCHASE_RESPONSE
import brs.api.http.common.ResultFields.QUANTITY_RESPONSE
import brs.api.http.common.ResultFields.REFUND_PLANCK_RESPONSE
import brs.api.http.common.ResultFields.SELLER_RESPONSE
import brs.api.http.common.ResultFields.TIMESTAMP_RESPONSE
import brs.services.ParameterService
import brs.util.json.safeGetAsBoolean
import brs.util.json.safeGetAsLong
import brs.util.json.safeGetAsString
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
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
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val mockEncryptedData = mock<EncryptedData>()

        whenever(mockEncryptedData.data).doReturn(byteArrayOf(1.toByte()))
        whenever(mockEncryptedData.nonce).doReturn(byteArrayOf(1.toByte()))

        val mockEncryptedDataList = mutableListOf(mockEncryptedData)

        val mockPurchase = mock<Purchase>()
        whenever(mockPurchase.id).doReturn(1L)
        whenever(mockPurchase.goodsId).doReturn(2L)
        whenever(mockPurchase.getName()).doReturn("name")
        whenever(mockPurchase.sellerId).doReturn(3L)
        whenever(mockPurchase.pricePlanck).doReturn(4L)
        whenever(mockPurchase.quantity).doReturn(5)
        whenever(mockPurchase.buyerId).doReturn(6L)
        whenever(mockPurchase.timestamp).doReturn(7)
        whenever(mockPurchase.deliveryDeadlineTimestamp).doReturn(8)
        whenever(mockPurchase.isPending).doReturn(true)
        whenever(mockPurchase.goodsIsText()).doReturn(true)
        whenever(mockPurchase.discountPlanck).doReturn(8L)
        whenever(mockPurchase.refundPlanck).doReturn(9L)
        whenever(mockPurchase.encryptedGoods).doReturn(mockEncryptedData)
        whenever(mockPurchase.feedbackNotes).doReturn(mockEncryptedDataList)
        whenever(mockPurchase.refundNote).doReturn(mockEncryptedData)
        whenever(mockPurchase.note).doReturn(mockEncryptedData)
        whenever(mockPurchase.getPublicFeedback()).doReturn(listOf("feedback"))

        whenever(mockParameterService.getPurchase(eq(request))).doReturn(mockPurchase)

        val result = t.processRequest(request) as JsonObject

        assertNotNull(result)

        assertEquals(mockPurchase.id.toString(), result.get(PURCHASE_RESPONSE).safeGetAsString())
        assertEquals(mockPurchase.goodsId.toString(), result.get(GOODS_RESPONSE).safeGetAsString())
        assertEquals(mockPurchase.getName(), result.get(NAME_RESPONSE).safeGetAsString())
        assertEquals(mockPurchase.sellerId.toString(), result.get(SELLER_RESPONSE).safeGetAsString())
        assertEquals(mockPurchase.pricePlanck.toString(), result.get(PRICE_PLANCK_RESPONSE).safeGetAsString())
        assertEquals(mockPurchase.quantity.toLong(), result.get(QUANTITY_RESPONSE).safeGetAsLong())
        assertEquals(mockPurchase.buyerId.toString(), result.get(BUYER_RESPONSE).safeGetAsString())
        assertEquals(mockPurchase.timestamp.toLong(), result.get(TIMESTAMP_RESPONSE).safeGetAsLong())
        assertEquals(mockPurchase.deliveryDeadlineTimestamp.toLong(), result.get(DELIVERY_DEADLINE_TIMESTAMP_RESPONSE).safeGetAsLong())
        assertEquals(mockPurchase.isPending, result.get(PENDING_RESPONSE).safeGetAsBoolean())
        assertEquals(mockPurchase.discountPlanck.toString(), result.get(DISCOUNT_PLANCK_RESPONSE).safeGetAsString())
        assertEquals(mockPurchase.refundPlanck.toString(), result.get(REFUND_PLANCK_RESPONSE).safeGetAsString())
    }

}
