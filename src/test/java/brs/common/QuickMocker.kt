package brs.common

import brs.Blockchain
import brs.Burst
import brs.fluxcapacitor.*
import brs.props.PropertyService
import brs.props.Props
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.mockito.ArgumentMatchers

import javax.servlet.http.HttpServletRequest
import java.util.ArrayList
import java.util.Arrays

import brs.http.common.Parameters.*
import brs.props.Prop
import com.nhaarman.mockitokotlin2.*
import io.mockk.every
import io.mockk.mockkStatic

object QuickMocker {

    fun defaultPropertyService() = mock<PropertyService> {
        onGeneric { get(any<Prop<Any>>()) } doAnswer {
            when (it.getArgument<Prop<Any>>(0).defaultValue) {
                is Boolean -> return@doAnswer false
                is Int -> return@doAnswer -1
                is String -> return@doAnswer ""
                is List<*> -> return@doAnswer emptyList<String>()
                else -> null
            }
        }
    }

    fun fluxCapacitorEnabledFunctionalities(vararg enabledToggles: FluxEnable): FluxCapacitor {
        mockkStatic(Burst::class)
        val mockCapacitor = mock<FluxCapacitor> {
            on { it.getValue(any<FluxValue<Boolean>>()) } doReturn false
            on { it.getValue(any<FluxValue<Boolean>>(), any()) } doReturn false
        }
        for (ft in enabledToggles) {
            whenever(mockCapacitor.getValue(eq(ft))).doReturn(true)
            whenever(mockCapacitor.getValue(eq(ft), any())).doReturn(true)
        }
        every { Burst.fluxCapacitor } returns mockCapacitor
        return mockCapacitor
    }

    fun latestValueFluxCapacitor(): FluxCapacitor {
        val blockchain = mock<Blockchain>()
        val propertyService = mock<PropertyService>()
        whenever(blockchain.height).doReturn(Integer.MAX_VALUE)
        whenever(propertyService.get(eq(Props.DEV_TESTNET))).doReturn(false)
        return FluxCapacitorImpl(blockchain, propertyService)
    }

    fun httpServletRequest(vararg parameters: MockParam): HttpServletRequest {
        val mockedRequest = mock<HttpServletRequest>()

        for (mp in parameters) {
            whenever(mockedRequest.getParameter(mp.key)).doReturn(mp.value)
        }

        return mockedRequest
    }

    fun httpServletRequestDefaultKeys(vararg parameters: MockParam): HttpServletRequest {
        val paramsWithKeys = ArrayList(listOf(MockParam(SECRET_PHRASE_PARAMETER, TestConstants.TEST_SECRET_PHRASE), MockParam(PUBLIC_KEY_PARAMETER, TestConstants.TEST_PUBLIC_KEY), MockParam(DEADLINE_PARAMETER, TestConstants.DEADLINE), MockParam(FEE_NQT_PARAMETER, TestConstants.FEE)))

        paramsWithKeys.addAll(listOf(*parameters))

        return httpServletRequest(*paramsWithKeys.toTypedArray())
    }

    fun jsonObject(vararg parameters: JSONParam): JsonObject {
        val mockedRequest = JsonObject()

        for (mp in parameters) {
            mockedRequest.add(mp.key, mp.value)
        }

        return mockedRequest
    }

    class MockParam(val key: String, val value: String?) {

        constructor(key: String, value: Int?) : this(key, "" + value)

        constructor(key: String, value: Long?) : this(key, "" + value)

        constructor(key: String, value: Boolean?) : this(key, "" + value)
    }

    class JSONParam(val key: String, val value: JsonElement)
}
