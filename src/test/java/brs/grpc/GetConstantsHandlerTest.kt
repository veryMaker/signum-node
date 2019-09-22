package brs.grpc

import brs.Genesis
import brs.transaction.TransactionType
import com.google.protobuf.Empty
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import java.io.IOException

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

@RunWith(JUnit4::class)
class GetConstantsHandlerTest : AbstractGrpcTest() {

    @Before
    fun setUpGetConstantsHandlerTest() {
        defaultBrsService()
    }

    @Test
    fun testGetConstantsHandler() {
        val constants = brsService!!.getConstants(Empty.getDefaultInstance())
        assertEquals(Genesis.CREATOR_ID, constants.genesisAccount)
        assertEquals(Genesis.GENESIS_BLOCK_ID, constants.genesisBlock)
        // TODO check max block size / payload length
        assertEquals(TransactionType.getTransactionTypes().size.toLong(), constants.transactionTypesList.size.toLong())
        constants.transactionTypesList.forEach { transactionType ->
            val subtypes = TransactionType.getTransactionTypes()[transactionType.getType().toByte()]
            assertNotNull("Transaction type " + transactionType.getType() + " does not exist", subtypes)
            assertEquals(TransactionType.getTypeDescription(transactionType.type.toByte()), transactionType.getDescription())
            assertEquals(subtypes!!.size.toLong(), transactionType.subtypesList.size.toLong())
            transactionType.getSubtypesList().forEach { subtype ->
                val transactionSubtype = subtypes[subtype.subtype.toByte()]
                assertNotNull("Transaction subtype " + transactionType.getType() + "," + subtype.getSubtype() + " does not exist", transactionSubtype)
                // Don't assume that its position in the map and its actual type are the same.
                assertEquals(transactionSubtype!!.subtype.toLong(), subtype.subtype.toLong())
                assertEquals(transactionSubtype.description, subtype.description)
            }
        }
    }
}
