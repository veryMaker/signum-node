package brs.common

import brs.Blockchain
import brs.DependencyProvider
import brs.fluxcapacitor.FluxCapacitor
import brs.fluxcapacitor.FluxCapacitorImpl
import brs.fluxcapacitor.FluxEnable
import brs.fluxcapacitor.FluxValue
import brs.http.common.Parameters.DEADLINE_PARAMETER
import brs.http.common.Parameters.FEE_NQT_PARAMETER
import brs.http.common.Parameters.PUBLIC_KEY_PARAMETER
import brs.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.props.Prop
import brs.props.PropertyService
import brs.props.Props
import brs.taskScheduler.CoroutineTaskScheduler
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.*
import javax.servlet.http.HttpServletRequest
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSupertypeOf

object QuickMocker {
    fun dependencyProvider(vararg dependencies: Any): DependencyProvider {
        val classToDependency = dependencies
            .map { it::class.createType() to it }
            .toMap()
        require(dependencies.size == classToDependency.size) { "Duplicate dependencies found (two or more dependencies of the same type were provided)" }

        val dp = DependencyProvider()
        val insertedDependencies = mutableListOf<KType>()

        dp::class.members.forEach { member ->
            if (member is KMutableProperty<*>) {
                classToDependency.forEach { (dependencyType, dependency) ->
                    if (member.returnType == dependencyType || member.returnType.isSupertypeOf(dependencyType)) {
                        member.setter.call(dp, dependency)
                        insertedDependencies.add(dependencyType)
                    }
                }
            }
        }
        require(insertedDependencies.size == classToDependency.size) {
            val notInsertedDependencies = classToDependency.keys.toMutableList()
            notInsertedDependencies.removeAll(insertedDependencies)
            "Not all dependencies can go into dependency provider, these types can't: $notInsertedDependencies"
        }
        dp.taskScheduler = CoroutineTaskScheduler()
        return dp
    }

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
        val mockCapacitor = mock<FluxCapacitor> {
            onGeneric { it.getValue(any<FluxValue<Boolean>>()) } doReturn false
            onGeneric { it.getValue(any<FluxValue<Boolean>>(), any()) } doReturn false
        }
        enabledToggles.forEach { ft ->
            whenever(mockCapacitor.getValue(eq(ft))).doReturn(true)
            whenever(mockCapacitor.getValue(eq(ft), any())).doReturn(true)
        }
        return mockCapacitor
    }

    fun latestValueFluxCapacitor(): FluxCapacitor {
        val blockchain = mock<Blockchain>()
        val propertyService = mock<PropertyService>()
        whenever(blockchain.height).doReturn(Integer.MAX_VALUE)
        whenever(propertyService.get(eq(Props.DEV_TESTNET))).doReturn(false)
        return FluxCapacitorImpl(dependencyProvider(blockchain, propertyService))
    }

    fun httpServletRequest(vararg parameters: MockParam): HttpServletRequest {
        val mockedRequest = mock<HttpServletRequest>()

        parameters.forEach { mp ->
            whenever(mockedRequest.getParameter(mp.key)).doReturn(mp.value)
        }

        return mockedRequest
    }

    fun httpServletRequestDefaultKeys(vararg parameters: MockParam): HttpServletRequest {
        val paramsWithKeys = mutableListOf(listOf(MockParam(SECRET_PHRASE_PARAMETER, TestConstants.TEST_SECRET_PHRASE), MockParam(PUBLIC_KEY_PARAMETER, TestConstants.TEST_PUBLIC_KEY), MockParam(DEADLINE_PARAMETER, TestConstants.DEADLINE), MockParam(FEE_NQT_PARAMETER, TestConstants.FEE)))

        paramsWithKeys.addAll(listOf(parameters.toList()))

        return httpServletRequest(*paramsWithKeys.flatten().toTypedArray())
    }

    fun jsonObject(vararg parameters: JSONParam): JsonObject {
        val mockedRequest = JsonObject()

        parameters.forEach { mp ->
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
