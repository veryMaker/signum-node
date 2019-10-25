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
import brs.util.safeGetAsBoolean
import brs.util.safeGetAsLong
import brs.util.safeGetAsString
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
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GetDGSGoodsTest : AbstractUnitTest() {

    private lateinit var t: GetDGSGoods

    private lateinit var mockDGSGoodsStoreService: DGSGoodsStoreService

    @Before
    fun setUp() {
        mockDGSGoodsStoreService = mock()

        t = GetDGSGoods(mockDGSGoodsStoreService)
    }

    @Test
    fun processRequest_getSellerGoods() {
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
        val mockGoodIterator = mockCollection(mockGood)

        whenever(mockDGSGoodsStoreService.getSellerGoods(eq(sellerId), eq(true), eq(firstIndex), eq(lastIndex)))
                .doReturn(mockGoodIterator)

        val fullResult = t.processRequest(request) as JsonObject
        assertNotNull(fullResult)

        val goodsList = fullResult.get(GOODS_RESPONSE) as JsonArray
        assertNotNull(goodsList)
        assertEquals(1, goodsList.size().toLong())

        val result = goodsList.get(0) as JsonObject
        assertNotNull(result)

        assertEquals("" + mockGood.id, result.get(GOODS_RESPONSE).safeGetAsString())
        assertEquals(mockGood.name, result.get(NAME_RESPONSE).safeGetAsString())
        assertEquals(mockGood.description, result.get(DESCRIPTION_RESPONSE).safeGetAsString())
        assertEquals(mockGood.quantity.toLong(), result.get(QUANTITY_RESPONSE).safeGetAsLong())
        assertEquals("" + mockGood.priceNQT, result.get(PRICE_NQT_RESPONSE).safeGetAsString())
        assertEquals("" + mockGood.sellerId, result.get(SELLER_PARAMETER).safeGetAsString())
        assertEquals(mockGood.tags, result.get(TAGS_RESPONSE).safeGetAsString())
        assertEquals(mockGood.isDelisted, result.get(DELISTED_RESPONSE).safeGetAsBoolean())
        assertEquals(mockGood.timestamp.toLong(), result.get(TIMESTAMP_RESPONSE).safeGetAsLong())
    }

    @Test
    fun processRequest_getAllGoods() {
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
        val mockGoodIterator = mockCollection(mockGood)

        whenever(mockDGSGoodsStoreService.getAllGoods(eq(firstIndex), eq(lastIndex)))
                .doReturn(mockGoodIterator)

        val fullResult = t.processRequest(request) as JsonObject
        assertNotNull(fullResult)

        val goodsList = fullResult.get(GOODS_RESPONSE) as JsonArray
        assertNotNull(goodsList)
        assertEquals(1, goodsList.size().toLong())

        val result = goodsList.get(0) as JsonObject
        assertNotNull(result)

        assertEquals("" + mockGood.id, result.get(GOODS_RESPONSE).safeGetAsString())
        assertEquals(mockGood.name, result.get(NAME_RESPONSE).safeGetAsString())
        assertEquals(mockGood.description, result.get(DESCRIPTION_RESPONSE).safeGetAsString())
        assertEquals(mockGood.quantity.toLong(), result.get(QUANTITY_RESPONSE).safeGetAsLong())
        assertEquals("" + mockGood.priceNQT, result.get(PRICE_NQT_RESPONSE).safeGetAsString())
        assertEquals("" + mockGood.sellerId, result.get(SELLER_PARAMETER).safeGetAsString())
        assertEquals(mockGood.tags, result.get(TAGS_RESPONSE).safeGetAsString())
        assertEquals(mockGood.isDelisted, result.get(DELISTED_RESPONSE).safeGetAsBoolean())
        assertEquals(mockGood.timestamp.toLong(), result.get(TIMESTAMP_RESPONSE).safeGetAsLong())
    }

    @Test
    fun processRequest_getGoodsInStock() {
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
        val mockGoodIterator = mockCollection(mockGood)

        whenever(mockDGSGoodsStoreService.getGoodsInStock(eq(firstIndex), eq(lastIndex)))
                .doReturn(mockGoodIterator)

        val fullResult = t.processRequest(request) as JsonObject
        assertNotNull(fullResult)

        val goodsList = fullResult.get(GOODS_RESPONSE) as JsonArray
        assertNotNull(goodsList)
        assertEquals(1, goodsList.size().toLong())

        val result = goodsList.get(0) as JsonObject
        assertNotNull(result)

        assertEquals("" + mockGood.id, result.get(GOODS_RESPONSE).safeGetAsString())
        assertEquals(mockGood.name, result.get(NAME_RESPONSE).safeGetAsString())
        assertEquals(mockGood.description, result.get(DESCRIPTION_RESPONSE).safeGetAsString())
        assertEquals(mockGood.quantity.toLong(), result.get(QUANTITY_RESPONSE).safeGetAsLong())
        assertEquals("" + mockGood.priceNQT, result.get(PRICE_NQT_RESPONSE).safeGetAsString())
        assertEquals("" + mockGood.sellerId, result.get(SELLER_PARAMETER).safeGetAsString())
        assertEquals(mockGood.tags, result.get(TAGS_RESPONSE).safeGetAsString())
        assertEquals(mockGood.isDelisted, result.get(DELISTED_RESPONSE).safeGetAsBoolean())
        assertEquals(mockGood.timestamp.toLong(), result.get(TIMESTAMP_RESPONSE).safeGetAsLong())
    }

    private fun mockGood(): Goods {
        val mockGood = mock<Goods>()

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
