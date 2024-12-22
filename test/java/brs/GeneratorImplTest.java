package brs;

import brs.common.QuickMocker;
import brs.common.TestConstants;
import brs.fluxcapacitor.FluxCapacitor;
import brs.fluxcapacitor.FluxValues;
import brs.services.TimeService;
import brs.util.Convert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(JUnit4.class)
public class GeneratorImplTest {
    private Generator generator;
    private Generator generatorLnTime;
    private Generator generatorPocPlus;

    private static final byte[] exampleGenSig = Convert.parseHexString("6ec823b5fd86c4aee9f7c3453cacaf4a43296f48ede77e70060ca8225c2855d0");
    private static final long exampleBaseTarget = 70312;
    private static final long exampleAverageCommitment = Constants.ONE_SIGNA * 10;
    private static final int exampleHeight = 500000;

    @Before
    public void setUpGeneratorTest() {
        Blockchain blockchain = mock(Blockchain.class);
        Block block = mock(Block.class);
        doReturn(block).when(blockchain).getLastBlock();
        doReturn(exampleGenSig).when(block).getGenerationSignature();
        doReturn(exampleHeight).when(block).getHeight();
        doReturn(exampleBaseTarget).when(block).getBaseTarget();
        doReturn(exampleBaseTarget).when(block).getCapacityBaseTarget();

        TimeService timeService = mock(TimeService.class);

        FluxCapacitor fluxCapacitor = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.POC2);
        FluxCapacitor fluxCapacitorLnTime = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.POC2, FluxValues.SODIUM);
        FluxCapacitor fluxCapacitorPocPlus = QuickMocker.fluxCapacitorEnabledFunctionalities(FluxValues.POC2, FluxValues.POC_PLUS);
        
        doReturn(240).when(fluxCapacitor).getValue(FluxValues.BLOCK_TIME);
        doReturn(240).when(fluxCapacitorLnTime).getValue(FluxValues.BLOCK_TIME);
        doReturn(240).when(fluxCapacitorPocPlus).getValue(FluxValues.BLOCK_TIME);

        generator = new GeneratorImpl(blockchain, null, null, timeService, fluxCapacitor);
        generatorLnTime = new GeneratorImpl(blockchain, null, null, timeService, fluxCapacitorLnTime);
        generatorPocPlus = new GeneratorImpl(blockchain, null, null, timeService, fluxCapacitorPocPlus);
    }

    @Test
    public void testGeneratorCalculateGenerationSignature() {
        byte[] genSig = generator.calculateGenerationSignature(exampleGenSig, TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED);
        assertEquals("ba6f11e2fd1d1eb0a956f92d090da1dd3595c3d888a4ff3b3222c913be6f45b5", Convert.toHexString(genSig));
    }

    @Test
    public void testGeneratorCalculateDeadline() {
    	BigInteger hit = generator.calculateHit(TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED, 0, exampleGenSig, generator.calculateScoop(exampleGenSig, exampleHeight), exampleHeight);
        BigInteger deadline = generator.calculateDeadline(hit, exampleBaseTarget, 0, 0, exampleHeight);
        assertEquals(BigInteger.valueOf(7157291745432L), deadline);
    }

    @Test
    public void testGeneratorCalculateSodiumDeadline() {
        BigInteger hit = generatorLnTime.calculateHit(TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED, 0, exampleGenSig, generator.calculateScoop(exampleGenSig, exampleHeight), exampleHeight);
        BigInteger deadline = generatorLnTime.calculateDeadline(hit, exampleBaseTarget, 0, 0, exampleHeight);
        assertEquals(BigInteger.valueOf(1296L), deadline);
    }

    @Test
    public void testGeneratorCalculateNextDeadline() {
        BigInteger hit = generatorPocPlus.calculateHit(TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED, 0, exampleGenSig, generator.calculateScoop(exampleGenSig, exampleHeight), exampleHeight);
        BigInteger deadline = generatorPocPlus.calculateDeadline(hit, exampleBaseTarget, 100*exampleAverageCommitment, exampleAverageCommitment, exampleHeight);
        assertEquals(BigInteger.valueOf(1205L), deadline);
    }

    @Test
    public void testGeneratorCalculateHit() {
        assertEquals(new BigInteger("14592422770739690569"), generator.calculateHit(TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED, 0, exampleGenSig, 0, exampleHeight));
        // Scoop data is the generation signature repeated - not intended to be acutal scoop data for the purpose of this test. It is twice as long as the gensig as this is the expected scoop size.
        assertEquals(new BigInteger("16142911724569013009"), generator.calculateHit(exampleGenSig, Convert.parseHexString("6ec823b5fd86c4aee9f7c3453cacaf4a43296f48ede77e70060ca8225c2855d06ec823b5fd86c4aee9f7c3453cacaf4a43296f48ede77e70060ca8225c2855d0")));
    }

    @Test
    public void testGeneratorAddNonce() {
        assertEquals(0, generator.getAllGenerators().size());
        generator.addNonce(TestConstants.TEST_SECRET_PHRASE, 0L);
        assertEquals(1, generator.getAllGenerators().size());
        Generator.GeneratorState generatorState = generator.getAllGenerators().iterator().next();
        assertNotNull(generatorState);
        assertEquals(BigInteger.valueOf(212350960282639L), generatorState.getDeadline());
        assertEquals(500001, generatorState.getBlock());
        assertEquals(TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED, (long) generatorState.getAccountId());
        assertArrayEquals(TestConstants.TEST_PUBLIC_KEY_BYTES, generatorState.getPublicKey());
    }
}
