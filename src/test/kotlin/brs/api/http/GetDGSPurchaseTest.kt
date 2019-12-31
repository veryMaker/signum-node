package brs.api.http

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
import brs.common.QuickMocker
import brs.entity.Purchase
import brs.services.ParameterService
import brs.util.json.safeGetAsBoolean
import brs.util.json.safeGetAsLong
import brs.util.json.safeGetAsString
import burst.kit.entity.BurstEncryptedMessage
import brs.util.jetty.get
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetDGSPurchaseTest {

    private lateinit var t: GetDGSPurchase

    private lateinit var mockParameterService: ParameterService

    @Before
    fun setUp() {
        mockParameterService = mockk(relaxed = true)

        t = GetDGSPurchase(mockParameterService)
    }


    @Test
    fun processRequest() {
        val request = QuickMocker.httpServletRequest()

        val mockEncryptedData = mockk<BurstEncryptedMessage>(relaxed = true)

        every { mockEncryptedData.data } returns byteArrayOf(1.toByte())
        every { mockEncryptedData.nonce } returns byteArrayOf(1.toByte())
        every { mockEncryptedData.isText } returns true

        val mockEncryptedDataList = mutableListOf(mockEncryptedData)

        val mockPurchase = mockk<Purchase>(relaxed = true)
        every { mockPurchase.id } returns 1L
        every { mockPurchase.goodsId } returns 2L
        every { mockPurchase.name } returns "name"
        every { mockPurchase.sellerId } returns 3L
        every { mockPurchase.pricePlanck } returns 4L
        every { mockPurchase.quantity } returns 5
        every { mockPurchase.buyerId } returns 6L
        every { mockPurchase.timestamp } returns 7
        every { mockPurchase.deliveryDeadlineTimestamp } returns 8
        every { mockPurchase.isPending } returns true
        every { mockPurchase.discountPlanck } returns 8L
        every { mockPurchase.refundPlanck } returns 9L
        every { mockPurchase.encryptedGoods } returns mockEncryptedData
        every { mockPurchase.feedbackNotes } returns mockEncryptedDataList
        every { mockPurchase.refundNote } returns mockEncryptedData
        every { mockPurchase.note } returns mockEncryptedData
        every { mockPurchase.publicFeedback } returns mutableListOf("feedback")

        every { mockParameterService.getPurchase(eq(request)) } returns mockPurchase

        val result = t.processRequest(request) as JsonObject

        assertNotNull(result)

        assertEquals(mockPurchase.id.toString(), result.get(PURCHASE_RESPONSE).safeGetAsString())
        assertEquals(mockPurchase.goodsId.toString(), result.get(GOODS_RESPONSE).safeGetAsString())
        assertEquals(mockPurchase.name, result.get(NAME_RESPONSE).safeGetAsString())
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
