package brs.api.http

import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.IN_STOCK_ONLY_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.Parameters.SELLER_PARAMETER
import brs.api.http.common.ResultFields.DELISTED_RESPONSE
import brs.api.http.common.ResultFields.DESCRIPTION_RESPONSE
import brs.api.http.common.ResultFields.GOODS_RESPONSE
import brs.api.http.common.ResultFields.NAME_RESPONSE
import brs.api.http.common.ResultFields.PRICE_PLANCK_RESPONSE
import brs.api.http.common.ResultFields.QUANTITY_RESPONSE
import brs.api.http.common.ResultFields.TAGS_RESPONSE
import brs.api.http.common.ResultFields.TIMESTAMP_RESPONSE
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.entity.Goods
import brs.services.DigitalGoodsStoreService
import brs.util.json.safeGetAsBoolean
import brs.util.json.safeGetAsLong
import brs.util.json.safeGetAsString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GetDGSGoodsTest : AbstractUnitTest() {

    private lateinit var t: GetDGSGoods

    private lateinit var mockDigitalGoodsStoreService: DigitalGoodsStoreService

    @Before
    fun setUp() {
        mockDigitalGoodsStoreService = mockk()

        t = GetDGSGoods(mockDigitalGoodsStoreService)
    }

    @Test
    fun processRequest_getSellerGoods() {
        val sellerId = 1L
        val firstIndex = 2
        val lastIndex = 3

        val request = QuickMocker.httpServletRequest(
                MockParam(SELLER_PARAMETER, sellerId.toString()),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex.toString()),
                MockParam(LAST_INDEX_PARAMETER, lastIndex.toString()),
                MockParam(IN_STOCK_ONLY_PARAMETER, "true")
        )

        val mockGood = mockGood()
        val mockGoodIterator = mockCollection(mockGood)

        every { mockDigitalGoodsStoreService.getSellerGoods(eq(sellerId), eq(true), eq(firstIndex), eq(lastIndex)) } returns mockGoodIterator

        val fullResult = t.processRequest(request) as JsonObject
        assertNotNull(fullResult)

        val goodsList = fullResult.get(GOODS_RESPONSE) as JsonArray
        assertNotNull(goodsList)
        assertEquals(1, goodsList.size().toLong())

        val result = goodsList.get(0) as JsonObject
        assertNotNull(result)

        assertEquals(mockGood.id.toString(), result.get(GOODS_RESPONSE).safeGetAsString())
        assertEquals(mockGood.name, result.get(NAME_RESPONSE).safeGetAsString())
        assertEquals(mockGood.description, result.get(DESCRIPTION_RESPONSE).safeGetAsString())
        assertEquals(mockGood.quantity.toLong(), result.get(QUANTITY_RESPONSE).safeGetAsLong())
        assertEquals(mockGood.pricePlanck.toString(), result.get(PRICE_PLANCK_RESPONSE).safeGetAsString())
        assertEquals(mockGood.sellerId.toString(), result.get(SELLER_PARAMETER).safeGetAsString())
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
                MockParam(SELLER_PARAMETER, sellerId.toString()),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex.toString()),
                MockParam(LAST_INDEX_PARAMETER, lastIndex.toString()),
                MockParam(IN_STOCK_ONLY_PARAMETER, "false")
        )

        val mockGood = mockGood()
        val mockGoodIterator = mockCollection(mockGood)

        every { mockDigitalGoodsStoreService.getAllGoods(eq(firstIndex), eq(lastIndex)) } returns mockGoodIterator

        val fullResult = t.processRequest(request) as JsonObject
        assertNotNull(fullResult)

        val goodsList = fullResult.get(GOODS_RESPONSE) as JsonArray
        assertNotNull(goodsList)
        assertEquals(1, goodsList.size().toLong())

        val result = goodsList.get(0) as JsonObject
        assertNotNull(result)

        assertEquals(mockGood.id.toString(), result.get(GOODS_RESPONSE).safeGetAsString())
        assertEquals(mockGood.name, result.get(NAME_RESPONSE).safeGetAsString())
        assertEquals(mockGood.description, result.get(DESCRIPTION_RESPONSE).safeGetAsString())
        assertEquals(mockGood.quantity.toLong(), result.get(QUANTITY_RESPONSE).safeGetAsLong())
        assertEquals(mockGood.pricePlanck.toString(), result.get(PRICE_PLANCK_RESPONSE).safeGetAsString())
        assertEquals(mockGood.sellerId.toString(), result.get(SELLER_PARAMETER).safeGetAsString())
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
                MockParam(SELLER_PARAMETER, sellerId.toString()),
                MockParam(FIRST_INDEX_PARAMETER, firstIndex.toString()),
                MockParam(LAST_INDEX_PARAMETER, lastIndex.toString()),
                MockParam(IN_STOCK_ONLY_PARAMETER, "true")
        )

        val mockGood = mockGood()
        val mockGoodIterator = mockCollection(mockGood)

        every { mockDigitalGoodsStoreService.getGoodsInStock(eq(firstIndex), eq(lastIndex)) } returns mockGoodIterator

        val fullResult = t.processRequest(request) as JsonObject
        assertNotNull(fullResult)

        val goodsList = fullResult.get(GOODS_RESPONSE) as JsonArray
        assertNotNull(goodsList)
        assertEquals(1, goodsList.size().toLong())

        val result = goodsList.get(0) as JsonObject
        assertNotNull(result)

        assertEquals(mockGood.id.toString(), result.get(GOODS_RESPONSE).safeGetAsString())
        assertEquals(mockGood.name, result.get(NAME_RESPONSE).safeGetAsString())
        assertEquals(mockGood.description, result.get(DESCRIPTION_RESPONSE).safeGetAsString())
        assertEquals(mockGood.quantity.toLong(), result.get(QUANTITY_RESPONSE).safeGetAsLong())
        assertEquals(mockGood.pricePlanck.toString(), result.get(PRICE_PLANCK_RESPONSE).safeGetAsString())
        assertEquals(mockGood.sellerId.toString(), result.get(SELLER_PARAMETER).safeGetAsString())
        assertEquals(mockGood.tags, result.get(TAGS_RESPONSE).safeGetAsString())
        assertEquals(mockGood.isDelisted, result.get(DELISTED_RESPONSE).safeGetAsBoolean())
        assertEquals(mockGood.timestamp.toLong(), result.get(TIMESTAMP_RESPONSE).safeGetAsLong())
    }

    private fun mockGood(): Goods {
        val mockGood = mockk<Goods>()

        every { mockGood.id } returns 1L
        every { mockGood.name } returns "name"
        every { mockGood.description } returns "description"
        every { mockGood.quantity } returns 2
        every { mockGood.pricePlanck } returns 3L
        every { mockGood.sellerId } returns 4L
        every { mockGood.tags } returns "tags"
        every { mockGood.isDelisted } returns true
        every { mockGood.timestamp } returns 5

        return mockGood
    }
}
