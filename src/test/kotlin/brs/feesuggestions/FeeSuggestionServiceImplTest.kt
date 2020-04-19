package brs.feesuggestions

import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.db.BlockchainStore
import brs.entity.Block
import brs.objects.Constants.FEE_QUANT
import brs.services.BlockchainProcessorService
import brs.services.impl.FeeSuggestionServiceImpl
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class FeeSuggestionServiceImplTest : AbstractUnitTest() {
    private lateinit var t: FeeSuggestionServiceImpl

    private lateinit var blockchainProcessorServiceMock: BlockchainProcessorService
    private lateinit var blockchainStoreMock: BlockchainStore

    private lateinit var listenerCapturingSlot: CapturingSlot<(Block) -> Unit>

    @Before
    fun setUp() {
        blockchainProcessorServiceMock = mockk(relaxed = true, relaxUnitFun = true)
        blockchainStoreMock = mockk(relaxed = true)

        listenerCapturingSlot = CapturingSlot()
        every { blockchainProcessorServiceMock.addListener(eq(BlockchainProcessorService.Event.AFTER_BLOCK_APPLY), capture(listenerCapturingSlot)) } just runs

        t = FeeSuggestionServiceImpl(
            QuickMocker.dependencyProvider(
                blockchainProcessorServiceMock,
                QuickMocker.mockDb(blockchainStoreMock)
            ), 5
        )
    }

    @Test
    fun getFeeSuggestion() {
        val mockBlock1 = mockk<Block>(relaxed = true)
        every { mockBlock1.transactions } returns mutableListOf()
        val mockBlock2 = mockk<Block>(relaxed = true)
        every { mockBlock2.transactions } returns listOf(mockk(relaxed = true))
        val mockBlock3 = mockk<Block>(relaxed = true)
        every { mockBlock3.transactions } returns listOf(mockk(relaxed = true))
        val mockBlock4 = mockk<Block>(relaxed = true)
        every { mockBlock4.transactions } returns listOf(mockk(relaxed = true))
        val mockBlock5 = mockk<Block>(relaxed = true)
        every { mockBlock5.transactions } returns listOf(mockk(relaxed = true))

        val mockBlocksIterator = mockCollection(mockBlock1, mockBlock2, mockBlock3, mockBlock4, mockBlock5)
        every { blockchainStoreMock.getLatestBlocks(eq(5)) } returns mockBlocksIterator

        listenerCapturingSlot.captured(mockBlock1)
        listenerCapturingSlot.captured(mockBlock2)
        listenerCapturingSlot.captured(mockBlock3)
        listenerCapturingSlot.captured(mockBlock4)
        listenerCapturingSlot.captured(mockBlock5)

        val feeSuggestionOne = t.giveFeeSuggestion()
        assertEquals(1 * FEE_QUANT, feeSuggestionOne.cheapFee)
        assertEquals(2 * FEE_QUANT, feeSuggestionOne.standardFee)
        assertEquals(2 * FEE_QUANT, feeSuggestionOne.priorityFee)

        val mockBlock6 = mockk<Block>(relaxed = true)
        every { mockBlock6.transactions } returns listOf(mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true))
        val mockBlock7 = mockk<Block>(relaxed = true)
        every { mockBlock7.transactions } returns listOf(mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true))
        val mockBlock8 = mockk<Block>(relaxed = true)
        every { mockBlock8.transactions } returns listOf(mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true))

        listenerCapturingSlot.captured(mockBlock6)
        val feeSuggestionTwo = t.giveFeeSuggestion()
        assertEquals(2 * FEE_QUANT, feeSuggestionTwo.cheapFee)
        assertEquals(3 * FEE_QUANT, feeSuggestionTwo.standardFee)
        assertEquals(5 * FEE_QUANT, feeSuggestionTwo.priorityFee)

        listenerCapturingSlot.captured(mockBlock7)
        val feeSuggestionThree = t.giveFeeSuggestion()
        assertEquals(2 * FEE_QUANT, feeSuggestionThree.cheapFee)
        assertEquals(4 * FEE_QUANT, feeSuggestionThree.standardFee)
        assertEquals(5 * FEE_QUANT, feeSuggestionThree.priorityFee)

        listenerCapturingSlot.captured(mockBlock8)
        val feeSuggestionFour = t.giveFeeSuggestion()
        assertEquals(2 * FEE_QUANT, feeSuggestionFour.cheapFee)
        assertEquals(4 * FEE_QUANT, feeSuggestionFour.standardFee)
        assertEquals(6 * FEE_QUANT, feeSuggestionFour.priorityFee)
    }
}
