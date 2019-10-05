package brs.http

import brs.DigitalGoodsStore
import brs.DigitalGoodsStore.Goods
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.IN_STOCK_ONLY_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.Parameters.SELLER_PARAMETER
import brs.http.common.ResultFields.DELISTED_RESPONSE
import brs.http.common.ResultFields.DESCRIPTION_RESPONSE
import brs.http.common.ResultFields.GOODS_RESPONSE
import brs.http.common.ResultFields.NAME_RESPONSE
import brs.http.common.ResultFields.PRICE_NQT_RESPONSE
import brs.http.common.ResultFields.QUANTITY_RESPONSE
import brs.http.common.ResultFields.TAGS_RESPONSE
import brs.http.common.ResultFields.TIMESTAMP_RESPONSE
import brs.services.DGSGoodsStoreService
import brs.util.JSON
import com.google.gson.JsonArray
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
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GetDGSGoodsTest : AbstractUnitTest() {

    private lateinit var t: GetDGSGoods

    private lateinit var mockDGSGoodsStoreService: DGSGoodsStoreService

    @Before
    fun setUp() {
        mockDGSGoodsStoreService = mock<DGSGoodsStoreService>()

        t = GetDGSGoods(mockDGSGoodsStoreService!!)
    }

    @Test
    fun processRequest_getSellerGoods() = runBlocking {
        val sellerId = 1L
        val firstIndex = 2
        val lastIndex = 3

        val request = QuickMocker.httpServletRequest(
                MockParam(SELLER_PARAMETER, "" + sellerId),
                MockParam(FIRST_INDEX_PARAMETER, "" + firstIndex),
                MockParam(LAST_INDEX_PARAMETER, "" + lastIndex),
                MockParam(IN_STOCK_ONLY_PARAMETER, "true")
        )

        val mockGood = mockGood()
        val mockGoodIterator = mockCollection<Goods>(mockGood)

        whenever(mockDGSGoodsStoreService!!.getSellerGoods(eq(sellerId), eq(true), eq(firstIndex), eq(lastIndex)))
                .doReturn(mockGoodIterator)

        val fullResult = t!!.processRequest(request) as JsonObject
        assertNotNull(fullResult)

        val goodsList = fullResult.get(GOODS_RESPONSE) as JsonArray
        assertNotNull(goodsList)
        assertEquals(1, goodsList.size().toLong())

        val result = goodsList.get(0) as JsonObject
        assertNotNull(result)

        assertEquals("" + mockGood.id, JSON.getAsString(result.get(GOODS_RESPONSE)))
        assertEquals(mockGood.name, JSON.getAsString(result.get(NAME_RESPONSE)))
        assertEquals(mockGood.description, JSON.getAsString(result.get(DESCRIPTION_RESPONSE)))
        assertEquals(mockGood.quantity.toLong(), JSON.getAsInt(result.get(QUANTITY_RESPONSE)).toLong())
        assertEquals("" + mockGood.priceNQT, JSON.getAsString(result.get(PRICE_NQT_RESPONSE)))
        assertEquals("" + mockGood.sellerId, JSON.getAsString(result.get(SELLER_PARAMETER)))
        assertEquals(mockGood.tags, JSON.getAsString(result.get(TAGS_RESPONSE)))
        assertEquals(mockGood.isDelisted, JSON.getAsBoolean(result.get(DELISTED_RESPONSE)))
        assertEquals(mockGood.timestamp.toLong(), JSON.getAsInt(result.get(TIMESTAMP_RESPONSE)).toLong())
    }

    @Test
    fun processRequest_getAllGoods() = runBlocking {
        val sellerId = 0L
        val firstIndex = 2
        val lastIndex = 3

        val request = QuickMocker.httpServletRequest(
                MockParam(SELLER_PARAMETER, "" + sellerId),
                MockParam(FIRST_INDEX_PARAMETER, "" + firstIndex),
                MockParam(LAST_INDEX_PARAMETER, "" + lastIndex),
                MockParam(IN_STOCK_ONLY_PARAMETER, "false")
        )

        val mockGood = mockGood()
        val mockGoodIterator = mockCollection<Goods>(mockGood)

        whenever(mockDGSGoodsStoreService!!.getAllGoods(eq(firstIndex), eq(lastIndex)))
                .doReturn(mockGoodIterator)

        val fullResult = t!!.processRequest(request) as JsonObject
        assertNotNull(fullResult)

        val goodsList = fullResult.get(GOODS_RESPONSE) as JsonArray
        assertNotNull(goodsList)
        assertEquals(1, goodsList.size().toLong())

        val result = goodsList.get(0) as JsonObject
        assertNotNull(result)

        assertEquals("" + mockGood.id, JSON.getAsString(result.get(GOODS_RESPONSE)))
        assertEquals(mockGood.name, JSON.getAsString(result.get(NAME_RESPONSE)))
        assertEquals(mockGood.description, JSON.getAsString(result.get(DESCRIPTION_RESPONSE)))
        assertEquals(mockGood.quantity.toLong(), JSON.getAsInt(result.get(QUANTITY_RESPONSE)).toLong())
        assertEquals("" + mockGood.priceNQT, JSON.getAsString(result.get(PRICE_NQT_RESPONSE)))
        assertEquals("" + mockGood.sellerId, JSON.getAsString(result.get(SELLER_PARAMETER)))
        assertEquals(mockGood.tags, JSON.getAsString(result.get(TAGS_RESPONSE)))
        assertEquals(mockGood.isDelisted, JSON.getAsBoolean(result.get(DELISTED_RESPONSE)))
        assertEquals(mockGood.timestamp.toLong(), JSON.getAsInt(result.get(TIMESTAMP_RESPONSE)).toLong())
    }

    @Test
    fun processRequest_getGoodsInStock() = runBlocking {
        val sellerId = 0L
        val firstIndex = 2
        val lastIndex = 3

        val request = QuickMocker.httpServletRequest(
                MockParam(SELLER_PARAMETER, "" + sellerId),
                MockParam(FIRST_INDEX_PARAMETER, "" + firstIndex),
                MockParam(LAST_INDEX_PARAMETER, "" + lastIndex),
                MockParam(IN_STOCK_ONLY_PARAMETER, "true")
        )

        val mockGood = mockGood()
        val mockGoodIterator = mockCollection<Goods>(mockGood)

        whenever(mockDGSGoodsStoreService!!.getGoodsInStock(eq(firstIndex), eq(lastIndex)))
                .doReturn(mockGoodIterator)

        val fullResult = t!!.processRequest(request) as JsonObject
        assertNotNull(fullResult)

        val goodsList = fullResult.get(GOODS_RESPONSE) as JsonArray
        assertNotNull(goodsList)
        assertEquals(1, goodsList.size().toLong())

        val result = goodsList.get(0) as JsonObject
        assertNotNull(result)

        assertEquals("" + mockGood.id, JSON.getAsString(result.get(GOODS_RESPONSE)))
        assertEquals(mockGood.name, JSON.getAsString(result.get(NAME_RESPONSE)))
        assertEquals(mockGood.description, JSON.getAsString(result.get(DESCRIPTION_RESPONSE)))
        assertEquals(mockGood.quantity.toLong(), JSON.getAsInt(result.get(QUANTITY_RESPONSE)).toLong())
        assertEquals("" + mockGood.priceNQT, JSON.getAsString(result.get(PRICE_NQT_RESPONSE)))
        assertEquals("" + mockGood.sellerId, JSON.getAsString(result.get(SELLER_PARAMETER)))
        assertEquals(mockGood.tags, JSON.getAsString(result.get(TAGS_RESPONSE)))
        assertEquals(mockGood.isDelisted, JSON.getAsBoolean(result.get(DELISTED_RESPONSE)))
        assertEquals(mockGood.timestamp.toLong(), JSON.getAsInt(result.get(TIMESTAMP_RESPONSE)).toLong())
    }

    private fun mockGood(): DigitalGoodsStore.Goods {
        val mockGood = mock<DigitalGoodsStore.Goods>()

        whenever(mockGood.id).doReturn(1L)
        whenever(mockGood.name).doReturn("name")
        whenever(mockGood.description).doReturn("description")
        whenever(mockGood.quantity).doReturn(2)
        whenever(mockGood.priceNQT).doReturn(3L)
        whenever(mockGood.sellerId).doReturn(4L)
        whenever(mockGood.tags).doReturn("tags")
        whenever(mockGood.isDelisted).doReturn(true)
        whenever(mockGood.timestamp).doReturn(5)

        return mockGood
    }
}
