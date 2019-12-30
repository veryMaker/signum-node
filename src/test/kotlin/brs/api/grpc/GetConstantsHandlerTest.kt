package brs.api.grpc

import brs.objects.Genesis
import brs.transaction.type.TransactionType
import com.google.protobuf.Empty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GetConstantsHandlerTest : AbstractGrpcTest() {

    @Before
    fun setUpGetConstantsHandlerTest() {
        defaultBrsService()
    }

    @Test
    fun testGetConstantsHandler() {
        val constants = brsService.getConstants(Empty.getDefaultInstance())
        assertEquals(Genesis.CREATOR_ID, constants.genesisAccount)
        assertEquals(Genesis.BLOCK_ID, constants.genesisBlock)
        // TODO check max block size / payload length
        assertEquals(dp.transactionTypes.size.toLong(), constants.transactionTypesList.size.toLong())
        constants.transactionTypesList.forEach { transactionType ->
            val subtypes = dp.transactionTypes[transactionType.type.toByte()]
            assertNotNull("Transaction type " + transactionType.type + " does not exist", subtypes)
            assertEquals(TransactionType.getTypeDescription(transactionType.type.toByte()), transactionType.description)
            assertEquals(subtypes!!.size.toLong(), transactionType.subtypesList.size.toLong())
            transactionType.subtypesList.forEach { subtype ->
                val transactionSubtype = subtypes[subtype.subtype.toByte()]
                assertNotNull("Transaction subtype " + transactionType.type + "," + subtype.subtype + " does not exist", transactionSubtype)
                // Don't assume that its position in the map and its actual type are the same.
                assertEquals(transactionSubtype!!.subtype.toLong(), subtype.subtype.toLong())
                assertEquals(transactionSubtype.description, subtype.description)
            }
        }
    }
}
