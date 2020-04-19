package brs.services.impl

import brs.common.QuickMocker
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.EscrowStore
import brs.db.MutableBatchEntityTable
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
    private lateinit var mockEscrowTable: MutableBatchEntityTable<Escrow>
    private lateinit var mockEscrowDbKeyFactory: LongKeyFactory<Escrow>
    private lateinit var blockchainServiceMock: BlockchainService
    private lateinit var aliasServiceMock: AliasService
    private lateinit var accountServiceMock: AccountService

    @Before
    fun setUp() {
        mockEscrowStore = mockk(relaxed = true)
        mockEscrowTable = mockk(relaxed = true)
        mockEscrowDbKeyFactory = mockk(relaxed = true)

        blockchainServiceMock = mockk(relaxed = true)
        aliasServiceMock = mockk(relaxed = true)
        accountServiceMock = mockk(relaxed = true)

        every { mockEscrowStore.decisionTable } returns mockk(relaxed = true)
        every { mockEscrowStore.escrowTable } returns mockEscrowTable
        every { mockEscrowStore.escrowDbKeyFactory } returns mockEscrowDbKeyFactory

        t = EscrowServiceImpl(QuickMocker.dependencyProvider(
            QuickMocker.mockDb(mockEscrowStore),
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

        val mockEscrowKey = mockk<BurstKey>(relaxed = true)
        val mockEscrow = mockk<Escrow>(relaxed = true)

        every { mockEscrowDbKeyFactory.newKey(eq(escrowId)) } returns mockEscrowKey
        every { mockEscrowTable.get(eq(mockEscrowKey)) } returns mockEscrow

        assertEquals(mockEscrow, t.getEscrowTransaction(escrowId))
    }
}
