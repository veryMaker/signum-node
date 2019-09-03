package brs.feesuggestions

import brs.Block
import brs.BlockchainProcessor
import brs.BlockchainProcessor.Event
import brs.Transaction
import brs.common.AbstractUnitTest
import brs.db.store.BlockchainStore
import org.junit.Before
import org.junit.Test

import java.util.ArrayList
import java.util.Arrays
import java.util.function.Consumer

import brs.Constants.FEE_QUANT
import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Assertions.assertEquals

class FeeSuggestionCalculatorTest : AbstractUnitTest() {

    private var t: FeeSuggestionCalculator? = null

    private var blockchainProcessorMock: BlockchainProcessor? = null
    private var blockchainStoreMock: BlockchainStore? = null

    private var listenerArgumentCaptor: KArgumentCaptor<(Block) -> Unit>? = null

    @Before
    fun setUp() {
        blockchainProcessorMock = mock<BlockchainProcessor>()
        blockchainStoreMock = mock<BlockchainStore>()

        listenerArgumentCaptor = argumentCaptor()
        whenever(blockchainProcessorMock!!.addListener(listenerArgumentCaptor!!.capture(), eq(Event.AFTER_BLOCK_APPLY))).doReturn(true)

        t = FeeSuggestionCalculator(blockchainProcessorMock!!, blockchainStoreMock, 5)
    }

    @Test
    fun getFeeSuggestion() {
        val mockBlock1 = mock<Block>()
        whenever(mockBlock1.transactions).doReturn(mutableListOf())
        val mockBlock2 = mock<Block>()
        whenever(mockBlock2.transactions).doReturn(listOf(mock<Transaction>()))
        val mockBlock3 = mock<Block>()
        whenever(mockBlock3.transactions).doReturn(listOf(mock<Transaction>()))
        val mockBlock4 = mock<Block>()
        whenever(mockBlock4.transactions).doReturn(listOf(mock<Transaction>()))
        val mockBlock5 = mock<Block>()
        whenever(mockBlock5.transactions).doReturn(listOf(mock<Transaction>()))

        val mockBlocksIterator = mockCollection<Block>(mockBlock1, mockBlock2, mockBlock3, mockBlock4, mockBlock5)
        whenever(blockchainStoreMock!!.getLatestBlocks(eq(5))).doReturn(mockBlocksIterator)

        listenerArgumentCaptor!!.firstValue.accept(mockBlock1)
        listenerArgumentCaptor!!.firstValue.accept(mockBlock2)
        listenerArgumentCaptor!!.firstValue.accept(mockBlock3)
        listenerArgumentCaptor!!.firstValue.accept(mockBlock4)
        listenerArgumentCaptor!!.firstValue.accept(mockBlock5)

        val feeSuggestionOne = t!!.giveFeeSuggestion()
        assertEquals(1 * FEE_QUANT, feeSuggestionOne.cheapFee)
        assertEquals(2 * FEE_QUANT, feeSuggestionOne.standardFee)
        assertEquals(2 * FEE_QUANT, feeSuggestionOne.priorityFee)

        val mockBlock6 = mock<Block>()
        whenever(mockBlock6.transactions).doReturn(listOf(mock<Transaction>(), mock<Transaction>(), mock<Transaction>(), mock<Transaction>()))
        val mockBlock7 = mock<Block>()
        whenever(mockBlock7.transactions).doReturn(listOf(mock<Transaction>(), mock<Transaction>(), mock<Transaction>(), mock<Transaction>()))
        val mockBlock8 = mock<Block>()
        whenever(mockBlock8.transactions).doReturn(listOf(mock<Transaction>(), mock<Transaction>(), mock<Transaction>(), mock<Transaction>(), mock<Transaction>()))

        listenerArgumentCaptor!!.firstValue.accept(mockBlock6)
        val feeSuggestionTwo = t!!.giveFeeSuggestion()
        assertEquals(2 * FEE_QUANT, feeSuggestionTwo.cheapFee)
        assertEquals(3 * FEE_QUANT, feeSuggestionTwo.standardFee)
        assertEquals(5 * FEE_QUANT, feeSuggestionTwo.priorityFee)

        listenerArgumentCaptor!!.firstValue.accept(mockBlock7)
        val feeSuggestionThree = t!!.giveFeeSuggestion()
        assertEquals(2 * FEE_QUANT, feeSuggestionThree.cheapFee)
        assertEquals(4 * FEE_QUANT, feeSuggestionThree.standardFee)
        assertEquals(5 * FEE_QUANT, feeSuggestionThree.priorityFee)

        listenerArgumentCaptor!!.firstValue.accept(mockBlock8)
        val feeSuggestionFour = t!!.giveFeeSuggestion()
        assertEquals(2 * FEE_QUANT, feeSuggestionFour.cheapFee)
        assertEquals(4 * FEE_QUANT, feeSuggestionFour.standardFee)
        assertEquals(6 * FEE_QUANT, feeSuggestionFour.priorityFee)
    }
}