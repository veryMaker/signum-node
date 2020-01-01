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
import brs.util.json.getMemberAsBoolean
import brs.util.json.getMemberAsLong
import brs.util.json.getMemberAsString
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
        mockDigitalGoodsStoreService = mockk(relaxed = true)

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

        assertEquals(mockGood.id.toString(), result.getMemberAsString(GOODS_RESPONSE))
        assertEquals(mockGood.name, result.getMemberAsString(NAME_RESPONSE))
        assertEquals(mockGood.description, result.getMemberAsString(DESCRIPTION_RESPONSE))
        assertEquals(mockGood.quantity.toLong(), result.getMemberAsLong(QUANTITY_RESPONSE))
        assertEquals(mockGood.pricePlanck.toString(), result.getMemberAsString(PRICE_PLANCK_RESPONSE))
        assertEquals(mockGood.sellerId.toString(), result.getMemberAsString(SELLER_PARAMETER))
        assertEquals(mockGood.tags, result.getMemberAsString(TAGS_RESPONSE))
        assertEquals(mockGood.isDelisted, result.getMemberAsBoolean(DELISTED_RESPONSE))
        assertEquals(mockGood.timestamp.toLong(), result.getMemberAsLong(TIMESTAMP_RESPONSE))
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

        assertEquals(mockGood.id.toString(), result.getMemberAsString(GOODS_RESPONSE))
        assertEquals(mockGood.name, result.getMemberAsString(NAME_RESPONSE))
        assertEquals(mockGood.description, result.getMemberAsString(DESCRIPTION_RESPONSE))
        assertEquals(mockGood.quantity.toLong(), result.getMemberAsLong(QUANTITY_RESPONSE))
        assertEquals(mockGood.pricePlanck.toString(), result.getMemberAsString(PRICE_PLANCK_RESPONSE))
        assertEquals(mockGood.sellerId.toString(), result.getMemberAsString(SELLER_PARAMETER))
        assertEquals(mockGood.tags, result.getMemberAsString(TAGS_RESPONSE))
        assertEquals(mockGood.isDelisted, result.getMemberAsBoolean(DELISTED_RESPONSE))
        assertEquals(mockGood.timestamp.toLong(), result.getMemberAsLong(TIMESTAMP_RESPONSE))
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

        assertEquals(mockGood.id.toString(), result.getMemberAsString(GOODS_RESPONSE))
        assertEquals(mockGood.name, result.getMemberAsString(NAME_RESPONSE))
        assertEquals(mockGood.description, result.getMemberAsString(DESCRIPTION_RESPONSE))
        assertEquals(mockGood.quantity.toLong(), result.getMemberAsLong(QUANTITY_RESPONSE))
        assertEquals(mockGood.pricePlanck.toString(), result.getMemberAsString(PRICE_PLANCK_RESPONSE))
        assertEquals(mockGood.sellerId.toString(), result.getMemberAsString(SELLER_PARAMETER))
        assertEquals(mockGood.tags, result.getMemberAsString(TAGS_RESPONSE))
        assertEquals(mockGood.isDelisted, result.getMemberAsBoolean(DELISTED_RESPONSE))
        assertEquals(mockGood.timestamp.toLong(), result.getMemberAsLong(TIMESTAMP_RESPONSE))
    }

    private fun mockGood(): Goods {
        val mockGood = mockk<Goods>(relaxed = true)

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
