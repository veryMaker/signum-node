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
import io.mockk.every
import io.mockk.mockk
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
        mockEscrowStore = mockk()
        mockEscrowTable = mockk()
        mockEscrowDbKeyFactory = mockk()

        blockchainServiceMock = mockk()
        aliasServiceMock = mockk()
        accountServiceMock = mockk()

        every { mockEscrowStore.decisionTable } returns mockk()
        every { mockEscrowStore.escrowTable } returns mockEscrowTable
        every { mockEscrowStore.escrowDbKeyFactory } returns mockEscrowDbKeyFactory

        t = EscrowServiceImpl(QuickMocker.dependencyProvider(
            mockEscrowStore,
            blockchainServiceMock,
            aliasServiceMock,
            accountServiceMock
        ))
    }


    @Test
    fun getAllEscrowTransactions() {
        val mockEscrowIterator = mockk<Collection<Escrow>>()

        every { mockEscrowTable.getAll(eq(0), eq(-1), any<Collection<SortField<*>>>()) } returns mockEscrowIterator

        assertEquals(mockEscrowIterator, t.getAllEscrowTransactions())
    }

    @Test
    fun getEscrowTransaction() {
        val escrowId = 123L

        val mockEscrowKey = mockk<BurstKey>()
        val mockEscrow = mockk<Escrow>()

        every { mockEscrowDbKeyFactory.newKey(eq(escrowId)) } returns mockEscrowKey
        every { mockEscrowTable.get(eq(mockEscrowKey)) } returns mockEscrow

        assertEquals(mockEscrow, t.getEscrowTransaction(escrowId))
    }
}
