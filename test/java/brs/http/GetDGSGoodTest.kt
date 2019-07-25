package brs.http

import brs.BurstException
import brs.DigitalGoodsStore
import brs.common.QuickMocker
import brs.services.ParameterService
import brs.util.JSON
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.http.common.ResultFields.*
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class GetDGSGoodTest {

    private var t: GetDGSGood? = null

    private var mockParameterService: ParameterService? = null

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()

        t = GetDGSGood(mockParameterService!!)
    }

    @Test
    @Throws(BurstException::class)
    fun processRequest() {
        val mockGoods = mock<DigitalGoodsStore.Goods>()
        whenever(mockGoods.id).thenReturn(1L)
        whenever(mockGoods.name).thenReturn("name")
        whenever(mockGoods.description).thenReturn("description")
        whenever(mockGoods.quantity).thenReturn(2)
        whenever(mockGoods.priceNQT).thenReturn(3L)
        whenever(mockGoods.tags).thenReturn("tags")
        whenever(mockGoods.isDelisted).thenReturn(true)
        whenever(mockGoods.timestamp).thenReturn(12345)

        val req = QuickMocker.httpServletRequest()

        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(req))).thenReturn(mockGoods)

        val result = t!!.processRequest(req) as JsonObject
        assertNotNull(result)

        assertEquals("" + mockGoods.id, JSON.getAsString(result.get(GOODS_RESPONSE)))
        assertEquals(mockGoods.name, JSON.getAsString(result.get(NAME_RESPONSE)))
        assertEquals(mockGoods.description, JSON.getAsString(result.get(DESCRIPTION_RESPONSE)))
        assertEquals(mockGoods.quantity.toLong(), JSON.getAsInt(result.get(QUANTITY_RESPONSE)).toLong())
        assertEquals("" + mockGoods.priceNQT, JSON.getAsString(result.get(PRICE_NQT_RESPONSE)))
        assertEquals(mockGoods.tags, JSON.getAsString(result.get(TAGS_RESPONSE)))
        assertEquals(mockGoods.isDelisted, JSON.getAsBoolean(result.get(DELISTED_RESPONSE)))
        assertEquals(mockGoods.timestamp.toLong(), JSON.getAsInt(result.get(TIMESTAMP_RESPONSE)).toLong())
    }
}