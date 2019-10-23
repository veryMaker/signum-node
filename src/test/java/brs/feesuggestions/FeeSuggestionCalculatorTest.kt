package brs.feesuggestions

import brs.Block
import brs.BlockchainProcessor
import brs.BlockchainProcessor.Event
import brs.Constants.FEE_QUANT
import brs.Transaction
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.db.store.BlockchainStore
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class FeeSuggestionCalculatorTest : AbstractUnitTest() {

    private lateinit var t: FeeSuggestionCalculator

    private lateinit var blockchainProcessorMock: BlockchainProcessor
    private lateinit var blockchainStoreMock: BlockchainStore

    private lateinit var listenerArgumentCaptor: KArgumentCaptor<suspend (Block) -> Unit>

    @Before
    fun setUp() = runBlocking {
        blockchainProcessorMock = mock()
        blockchainStoreMock = mock()

        listenerArgumentCaptor = argumentCaptor()
        whenever(blockchainProcessorMock.addListener(eq(Event.AFTER_BLOCK_APPLY), listenerArgumentCaptor.capture())).doReturn(Unit) // TODO ??

        t = FeeSuggestionCalculator(QuickMocker.dependencyProvider(blockchainProcessorMock, blockchainStoreMock), 5)
    }

    @Test
    fun getFeeSuggestion() = runBlocking {
        val mockBlock1 = mock<Block>()
        whenever(mockBlock1.getTransactions()).doReturn(mutableListOf())
        val mockBlock2 = mock<Block>()
        whenever(mockBlock2.getTransactions()).doReturn(listOf(mock()))
        val mockBlock3 = mock<Block>()
        whenever(mockBlock3.getTransactions()).doReturn(listOf(mock()))
        val mockBlock4 = mock<Block>()
        whenever(mockBlock4.getTransactions()).doReturn(listOf(mock()))
        val mockBlock5 = mock<Block>()
        whenever(mockBlock5.getTransactions()).doReturn(listOf(mock()))

        val mockBlocksIterator = mockCollection(mockBlock1, mockBlock2, mockBlock3, mockBlock4, mockBlock5)
        whenever(blockchainStoreMock.getLatestBlocks(eq(5))).doReturn(mockBlocksIterator)

        listenerArgumentCaptor.firstValue(mockBlock1)
        listenerArgumentCaptor.firstValue(mockBlock2)
        listenerArgumentCaptor.firstValue(mockBlock3)
        listenerArgumentCaptor.firstValue(mockBlock4)
        listenerArgumentCaptor.firstValue(mockBlock5)

        val feeSuggestionOne = t.giveFeeSuggestion()
        assertEquals(1 * FEE_QUANT, feeSuggestionOne.cheapFee)
        assertEquals(2 * FEE_QUANT, feeSuggestionOne.standardFee)
        assertEquals(2 * FEE_QUANT, feeSuggestionOne.priorityFee)

        val mockBlock6 = mock<Block>()
        whenever(mockBlock6.getTransactions()).doReturn(listOf(mock(), mock(), mock(), mock()))
        val mockBlock7 = mock<Block>()
        whenever(mockBlock7.getTransactions()).doReturn(listOf(mock(), mock(), mock(), mock()))
        val mockBlock8 = mock<Block>()
        whenever(mockBlock8.getTransactions()).doReturn(listOf(mock(), mock(), mock(), mock(), mock()))

        listenerArgumentCaptor.firstValue(mockBlock6)
        val feeSuggestionTwo = t.giveFeeSuggestion()
        assertEquals(2 * FEE_QUANT, feeSuggestionTwo.cheapFee)
        assertEquals(3 * FEE_QUANT, feeSuggestionTwo.standardFee)
        assertEquals(5 * FEE_QUANT, feeSuggestionTwo.priorityFee)

        listenerArgumentCaptor.firstValue(mockBlock7)
        val feeSuggestionThree = t.giveFeeSuggestion()
        assertEquals(2 * FEE_QUANT, feeSuggestionThree.cheapFee)
        assertEquals(4 * FEE_QUANT, feeSuggestionThree.standardFee)
        assertEquals(5 * FEE_QUANT, feeSuggestionThree.priorityFee)

        listenerArgumentCaptor.firstValue(mockBlock8)
        val feeSuggestionFour = t.giveFeeSuggestion()
        assertEquals(2 * FEE_QUANT, feeSuggestionFour.cheapFee)
        assertEquals(4 * FEE_QUANT, feeSuggestionFour.standardFee)
        assertEquals(6 * FEE_QUANT, feeSuggestionFour.priorityFee)
    }
}