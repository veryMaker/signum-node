package brs.common

import brs.api.http.common.Parameters.DEADLINE_PARAMETER
import brs.api.http.common.Parameters.FEE_PLANCK_PARAMETER
import brs.api.http.common.Parameters.PUBLIC_KEY_PARAMETER
import brs.api.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.entity.DependencyProvider
import brs.entity.FluxEnable
import brs.entity.FluxValue
import brs.entity.Prop
import brs.objects.Props
import brs.services.BlockchainService
import brs.services.FluxCapacitorService
import brs.services.PropertyService
import brs.services.impl.FluxCapacitorServiceImpl
import brs.services.impl.RxJavaTaskSchedulerService
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
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

    fun defaultPropertyService() = mockk<PropertyService> {
        every { get(any<Prop<Any>>()) } answers {
            when ((args[0] as Prop<*>).defaultValue) {
                is Boolean -> false
                is Int -> -1
                is String -> ""
                is List<*> -> emptyList<String>()
                else -> ""
            }
        }
    }

    fun fluxCapacitorEnabledFunctionalities(vararg enabledToggles: FluxEnable): FluxCapacitorService {
        val mockCapacitor = mockk<FluxCapacitorService> {
            every { getValue(any<FluxValue<Boolean>>()) } returns false
            every { getValue(any<FluxValue<Boolean>>(), any()) } returns false
        }
        enabledToggles.forEach { ft ->
            every { mockCapacitor.getValue(eq(ft)) } returns true
            every { mockCapacitor.getValue(eq(ft), any()) } returns true
        }
        return mockCapacitor
    }

    fun latestValueFluxCapacitor(): FluxCapacitorService {
        val blockchain = mockk<BlockchainService>(relaxed = true)
        val propertyService = mockk<PropertyService>(relaxed = true)
        every { blockchain.height } returns Integer.MAX_VALUE
        every { propertyService.get(eq(Props.DEV_TESTNET)) } returns false
        return FluxCapacitorServiceImpl(dependencyProvider(blockchain, propertyService))
    }

    fun httpServletRequest(vararg parameters: MockParam): HttpServletRequest {
        val mockedRequest = mockk<HttpServletRequest>(relaxed = true)

        parameters.forEach { mp ->
            every { mockedRequest.getParameter(mp.key) } returns mp.value
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
