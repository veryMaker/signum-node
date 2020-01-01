package brs

import brs.common.QuickMocker
import brs.common.TestConstants
import brs.entity.Block
import brs.objects.FluxValues
import brs.services.BlockchainService
import brs.services.TimeService
import brs.services.impl.GeneratorServiceImpl
import brs.util.convert.parseHexString
import brs.util.convert.toHexString
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.math.BigInteger

@RunWith(JUnit4::class)
class GeneratorServiceImplTest {
    private lateinit var generatorService: GeneratorServiceImpl

    @Before
    fun setUpGeneratorTest() {
        val blockchain = mockk<BlockchainService>(relaxed = true)
        val block = mockk<Block>(relaxed = true)
        every { blockchain.lastBlock } returns block
        every { block.generationSignature } returns exampleGenSig
        every { block.height } returns exampleHeight
        every { block.baseTarget } returns exampleBaseTarget

        val timeService = mockk<TimeService>(relaxed = true)

        val fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.POC2)

        generatorService = GeneratorServiceImpl(
            QuickMocker.dependencyProvider(
                blockchain,
                timeService,
                fluxCapacitor
            )
        )
    }

    @Test
    fun testGeneratorCalculateGenerationSignature() {
        val genSig = generatorService.calculateGenerationSignature(exampleGenSig, TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED)
        assertEquals("ba6f11e2fd1d1eb0a956f92d090da1dd3595c3d888a4ff3b3222c913be6f45b5", genSig.toHexString())
    }

    @Test
    fun btestGeneratorCalculateDeadline() {
        val deadline = generatorService.calculateDeadline(TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED, 0,
            exampleGenSig, generatorService.calculateScoop(exampleGenSig, exampleHeight.toLong()), exampleBaseTarget, exampleHeight)
        assertEquals(BigInteger.valueOf(7157291745432L), deadline)
    }

    @Test
    fun testGeneratorCalculateHit() {
        assertEquals(BigInteger("14592422770739690569"), generatorService.calculateHit(TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED, 0,
            exampleGenSig, 0, exampleHeight))
        // Scoop data is the generation signature repeated - not intended to be acutal scoop data for the purpose of this test. It is twice as long as the gensig as this is the expected scoop size.
        assertEquals(BigInteger("16142911724569013009"), generatorService.calculateHit(TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED, 0, exampleGenSig, "6ec823b5fd86c4aee9f7c3453cacaf4a43296f48ede77e70060ca8225c2855d06ec823b5fd86c4aee9f7c3453cacaf4a43296f48ede77e70060ca8225c2855d0".parseHexString()))
    }

    @Test
    fun testGeneratorAddNonce() {
        assertEquals(0, generatorService.numberOfGenerators.toLong())
        generatorService.addNonce(TestConstants.TEST_SECRET_PHRASE, 0L)
        assertEquals(1, generatorService.numberOfGenerators.toLong())
        val generatorState = generatorService.generators.values.first()
        assertNotNull(generatorState)
        assertEquals(BigInteger.valueOf(212350960282639L), generatorState.deadline)
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
