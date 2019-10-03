package brs

import brs.common.QuickMocker
import brs.common.TestConstants
import brs.fluxcapacitor.FluxValues
import brs.props.PropertyService
import brs.props.Props
import brs.services.TimeService
import brs.util.convert.parseHexString
import brs.util.convert.toHexString
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import java.math.BigInteger

import org.junit.Assert.*

@RunWith(JUnit4::class)
class MockGeneratorTest {
    private var generator: Generator? = null

    @Before
    fun setUpGeneratorTest() {
        val blockchain = mock<Blockchain>()
        val block = mock<Block>()
        doReturn(block).whenever(blockchain).lastBlock
        doReturn(exampleGenSig).whenever(block).generationSignature
        doReturn(exampleHeight).whenever(block).height
        doReturn(exampleBaseTarget).whenever(block).baseTarget

        val timeService = mock<TimeService>()

        val fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.POC2)
        val propertyService = mock<PropertyService>()
        doReturn(1000).whenever(propertyService).get(Props.DEV_MOCK_MINING_DEADLINE)

        generator = GeneratorImpl.MockGenerator(QuickMocker.dependencyProvider(propertyService, blockchain, timeService, fluxCapacitor))
    }

    @Test
    fun testGeneratorCalculateGenerationSignature() {
        val genSig = generator!!.calculateGenerationSignature(exampleGenSig!!, TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED)
        assertEquals("ba6f11e2fd1d1eb0a956f92d090da1dd3595c3d888a4ff3b3222c913be6f45b5", genSig.toHexString())
    }

    @Test
    fun testGeneratorCalculateDeadline() {
        val deadline = generator!!.calculateDeadline(TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED, 0, exampleGenSig!!, generator!!.calculateScoop(exampleGenSig, exampleHeight.toLong()), exampleBaseTarget, exampleHeight)
        assertEquals(BigInteger.valueOf(1000L), deadline)
    }

    @Test
    fun testGeneratorCalculateHit() {
        assertEquals(BigInteger.valueOf(1000L), generator!!.calculateHit(TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED, 0, exampleGenSig!!, 0, exampleHeight))
        // Scoop data is the generation signature repeated - not intended to be acutal scoop data for the purpose of this test. It is twice as long as the gensig as this is the expected scoop size.
        assertEquals(BigInteger.valueOf(1000L), generator!!.calculateHit(TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED, 0, exampleGenSig, "6ec823b5fd86c4aee9f7c3453cacaf4a43296f48ede77e70060ca8225c2855d06ec823b5fd86c4aee9f7c3453cacaf4a43296f48ede77e70060ca8225c2855d0".parseHexString()))
    }

    @Test
    fun testGeneratorAddNonce() {
        assertEquals(0, generator!!.allGenerators.size.toLong())
        generator!!.addNonce(TestConstants.TEST_SECRET_PHRASE, 0L)
        assertEquals(1, generator!!.allGenerators.size.toLong())
        val generatorState = generator!!.allGenerators.iterator().next()
        assertNotNull(generatorState)
        assertEquals(BigInteger.valueOf(1000), generatorState.deadline)
        assertEquals(500001, generatorState.block)
        assertEquals(TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED, generatorState.accountId as Long)
        assertArrayEquals(TestConstants.TEST_PUBLIC_KEY_BYTES, generatorState.publicKey)
    }

    companion object {

        private val exampleGenSig = "6ec823b5fd86c4aee9f7c3453cacaf4a43296f48ede77e70060ca8225c2855d0".parseHexString()
        private const val exampleBaseTarget: Long = 70312
        private const val exampleHeight = 500000
    }
}
