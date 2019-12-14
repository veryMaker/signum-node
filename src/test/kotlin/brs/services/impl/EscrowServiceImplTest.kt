package brs.services.impl

import brs.common.QuickMocker
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.EscrowStore
import brs.db.VersionedEntityTable
import brs.entity.Escrow
import brs.services.AccountService
import brs.services.AliasService
import brs.services.BlockchainService
import com.nhaarman.mockitokotlin2.*
import org.jooq.SortField
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class EscrowServiceImplTest {

    private lateinit var t: EscrowServiceImpl

    private lateinit var mockEscrowStore: EscrowStore
    private lateinit var mockEscrowTable: VersionedEntityTable<Escrow>
    private lateinit var mockEscrowDbKeyFactory: LongKeyFactory<Escrow>
    private lateinit var blockchainServiceMock: BlockchainService
    private lateinit var aliasServiceMock: AliasService
    private lateinit var accountServiceMock: AccountService

    @Before
    fun setUp() {
        mockEscrowStore = mock()
        mockEscrowTable = mock()
        mockEscrowDbKeyFactory = mock()

        blockchainServiceMock = mock()
        aliasServiceMock = mock()
        accountServiceMock = mock()

        whenever(mockEscrowStore.escrowTable).doReturn(mockEscrowTable)
        whenever(mockEscrowStore.escrowDbKeyFactory).doReturn(mockEscrowDbKeyFactory)

        t = EscrowServiceImpl(QuickMocker.dependencyProvider(
            mockEscrowStore,
            blockchainServiceMock,
            aliasServiceMock,
            accountServiceMock
        ))
    }


    @Test
    fun getAllEscrowTransactions() {
        val mockEscrowIterator = mock<Collection<Escrow>>()

        whenever(mockEscrowTable.getAll(eq(0), eq(-1), any<Collection<SortField<*>>>())).doReturn(mockEscrowIterator)

        assertEquals(mockEscrowIterator, t.getAllEscrowTransactions())
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
