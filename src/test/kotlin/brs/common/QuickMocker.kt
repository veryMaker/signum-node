package brs.common

import brs.services.BlockchainService
import brs.DependencyProvider
import brs.services.FluxCapacitorService
import brs.services.impl.FluxCapacitorServiceImpl
import brs.entity.FluxEnable
import brs.entity.FluxValue
import brs.api.http.common.Parameters.DEADLINE_PARAMETER
import brs.api.http.common.Parameters.FEE_PLANCK_PARAMETER
import brs.api.http.common.Parameters.PUBLIC_KEY_PARAMETER
import brs.api.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.entity.Prop
import brs.services.PropertyService
import brs.objects.Props
import brs.services.impl.RxJavaTaskSchedulerService
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
        dp.taskSchedulerService = RxJavaTaskSchedulerService()
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

    fun fluxCapacitorEnabledFunctionalities(vararg enabledToggles: FluxEnable): FluxCapacitorService {
        val mockCapacitor = mock<FluxCapacitorService> {
            onGeneric { it.getValue(any<FluxValue<Boolean>>()) } doReturn false
            onGeneric { it.getValue(any<FluxValue<Boolean>>(), any()) } doReturn false
        }
        enabledToggles.forEach { ft ->
            whenever(mockCapacitor.getValue(eq(ft))).doReturn(true)
            whenever(mockCapacitor.getValue(eq(ft), any())).doReturn(true)
        }
        return mockCapacitor
    }

    fun latestValueFluxCapacitor(): FluxCapacitorService {
        val blockchain = mock<BlockchainService>()
        val propertyService = mock<PropertyService>()
        whenever(blockchain.height).doReturn(Integer.MAX_VALUE)
        whenever(propertyService.get(eq(Props.DEV_TESTNET))).doReturn(false)
        return FluxCapacitorServiceImpl(dependencyProvider(blockchain, propertyService))
    }

    fun httpServletRequest(vararg parameters: MockParam): HttpServletRequest {
        val mockedRequest = mock<HttpServletRequest>()

        parameters.forEach { mp ->
            whenever(mockedRequest.getParameter(mp.key)).doReturn(mp.value)
        }

        return mockedRequest
    }

    fun httpServletRequestDefaultKeys(vararg parameters: MockParam): HttpServletRequest {
        val paramsWithKeys = mutableListOf(listOf(MockParam(SECRET_PHRASE_PARAMETER, TestConstants.TEST_SECRET_PHRASE), MockParam(PUBLIC_KEY_PARAMETER, TestConstants.TEST_PUBLIC_KEY), MockParam(DEADLINE_PARAMETER, TestConstants.DEADLINE), MockParam(FEE_PLANCK_PARAMETER, TestConstants.FEE)))

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

        constructor(key: String, value: Int?) : this(key, value.toString())

        constructor(key: String, value: Long?) : this(key, value.toString())

        constructor(key: String, value: Boolean?) : this(key, value.toString())
    }

    class JSONParam(val key: String, val value: JsonElement)
}
