package brs.services.impl

import brs.Blockchain
import brs.Escrow
import brs.common.QuickMocker
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.VersionedEntityTable
import brs.db.store.EscrowStore
import brs.services.AccountService
import brs.services.AliasService
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test

import org.junit.Assert.assertEquals

class EscrowServiceImplTest {

    private lateinit var t: EscrowServiceImpl

    private lateinit var mockEscrowStore: EscrowStore
    private lateinit var mockEscrowTable: VersionedEntityTable<Escrow>
    private lateinit var mockEscrowDbKeyFactory: LongKeyFactory<Escrow>
    private lateinit var blockchainMock: Blockchain
    private lateinit var aliasServiceMock: AliasService
    private lateinit var accountServiceMock: AccountService

    @Before
    fun setUp() {
        mockEscrowStore = mock()
        mockEscrowTable = mock()
        mockEscrowDbKeyFactory = mock()

        blockchainMock = mock()
        aliasServiceMock = mock()
        accountServiceMock = mock()

        whenever(mockEscrowStore.escrowTable).doReturn(mockEscrowTable)
        whenever(mockEscrowStore.escrowDbKeyFactory).doReturn(mockEscrowDbKeyFactory)

        t = EscrowServiceImpl(QuickMocker.dependencyProvider(
            mockEscrowStore,
            blockchainMock,
            aliasServiceMock,
            accountServiceMock
        ))
    }


    @Test
    fun getAllEscrowTransactions() {
        val mockEscrowIterator = mock<Collection<Escrow>>()

        whenever(mockEscrowTable.getAll(eq(0), eq(-1))).doReturn(mockEscrowIterator)

        assertEquals(mockEscrowIterator, t.allEscrowTransactions)
    }

    @Test
    fun getEscrowTransaction() {
        val escrowId = 123L

        val mockEscrowKey = mock<BurstKey>()
        val mockEscrow = mock<Escrow>()

        whenever(mockEscrowDbKeyFactory.newKey(eq(escrowId))).doReturn(mockEscrowKey)
        whenever(mockEscrowTable.get(eq(mockEscrowKey))).doReturn(mockEscrow)

        assertEquals(mockEscrow, t.getEscrowTransaction(escrowId))
    }
}
