package brs.http

import brs.DigitalGoodsStore
import brs.common.QuickMocker
import brs.http.common.ResultFields.DELISTED_RESPONSE
import brs.http.common.ResultFields.DESCRIPTION_RESPONSE
import brs.http.common.ResultFields.GOODS_RESPONSE
import brs.http.common.ResultFields.NAME_RESPONSE
import brs.http.common.ResultFields.PRICE_NQT_RESPONSE
import brs.http.common.ResultFields.QUANTITY_RESPONSE
import brs.http.common.ResultFields.TAGS_RESPONSE
import brs.http.common.ResultFields.TIMESTAMP_RESPONSE
import brs.services.ParameterService
import brs.util.JSON
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
import javax.servlet.http.HttpServletRequest

class GetDGSGoodTest {

    private lateinit var t: GetDGSGood

    private lateinit var mockParameterService: ParameterService

    @Before
    fun setUp() {
        mockParameterService = mock<ParameterService>()

        t = GetDGSGood(mockParameterService!!)
    }

    @Test
    fun processRequest() = runBlocking {
        val mockGoods = mock<DigitalGoodsStore.Goods>()
        whenever(mockGoods.id).doReturn(1L)
        whenever(mockGoods.name).doReturn("name")
        whenever(mockGoods.description).doReturn("description")
        whenever(mockGoods.quantity).doReturn(2)
        whenever(mockGoods.priceNQT).doReturn(3L)
        whenever(mockGoods.tags).doReturn("tags")
        whenever(mockGoods.isDelisted).doReturn(true)
        whenever(mockGoods.timestamp).doReturn(12345)

        val request = QuickMocker.httpServletRequest()

        whenever(mockParameterService!!.getGoods(eq<HttpServletRequest>(request))).doReturn(mockGoods)

        val result = t!!.processRequest(request) as JsonObject
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