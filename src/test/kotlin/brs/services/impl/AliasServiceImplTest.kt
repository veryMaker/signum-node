package brs.services.impl

import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.db.AliasStore
import brs.db.BurstKey
import brs.db.VersionedEntityTable
import brs.entity.Alias
import brs.entity.Alias.Offer
import brs.entity.Transaction
import brs.transaction.appendix.Attachment.MessagingAliasAssignment
import brs.transaction.appendix.Attachment.MessagingAliasSell
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class AliasServiceImplTest : AbstractUnitTest() {

    private lateinit var t: AliasServiceImpl

    private lateinit var aliasStoreMock: AliasStore
    private lateinit var aliasTableMock: VersionedEntityTable<Alias>
    private lateinit var aliasDbKeyFactoryMock: BurstKey.LongKeyFactory<Alias>
    private lateinit var offerTableMock: VersionedEntityTable<Offer>
    private lateinit var offerDbKeyFactoryMock: BurstKey.LongKeyFactory<Offer>

    @Before
    fun setUp() {
        aliasStoreMock = mockk()
        aliasTableMock = mockk()
        aliasDbKeyFactoryMock = mockk()
        offerTableMock = mockk()
        offerDbKeyFactoryMock = mockk()

        every { aliasStoreMock.aliasTable } returns aliasTableMock
        every { aliasStoreMock.aliasDbKeyFactory } returns aliasDbKeyFactoryMock
        every { aliasStoreMock.offerTable } returns offerTableMock
        every { aliasStoreMock.offerDbKeyFactory } returns offerDbKeyFactoryMock
        every { aliasDbKeyFactoryMock.newKey(any<Long>()) } returns mockk()

        t = AliasServiceImpl(QuickMocker.dependencyProvider(aliasStoreMock))
    }

    @Test
    fun getAlias() {
        val aliasName = "aliasName"
        val mockAlias = mockk<Alias>()

        every { aliasStoreMock.getAlias(eq(aliasName)) } returns mockAlias

        assertEquals(mockAlias, t.getAlias(aliasName))
    }

    @Test
    fun getAlias_byId() {
        val id = 123L
        val mockKey = mockk<BurstKey>()
        val mockAlias = mockk<Alias>()

        every { aliasDbKeyFactoryMock.newKey(eq(id)) } returns mockKey
        every { aliasTableMock[eq(mockKey)] } returns mockAlias

        assertEquals(mockAlias, t.getAlias(id))
    }

    @Test
    fun getOffer() {
        val aliasId = 123L
        val mockAlias = mockk<Alias>()
        every { mockAlias.id } returns aliasId
        val mockOfferKey = mockk<BurstKey>()
        val mockOffer = mockk<Offer>()

        every { offerDbKeyFactoryMock.newKey(eq(aliasId)) } returns mockOfferKey
        every { offerTableMock[eq(mockOfferKey)] } returns mockOffer

        assertEquals(mockOffer, t.getOffer(mockAlias))
    }

    @Test
    fun getAliasCount() {
        every { aliasTableMock.count } returns 5
        assertEquals(5L, t.getAliasCount())
    }

    @Test
    fun getAliasesByOwner() {
        val accountId = 123L
        val from = 0
        val to = 1

        val mockAliasIterator = mockCollection<Alias>()

        every { aliasStoreMock.getAliasesByOwner(eq(accountId), eq(from), eq(to)) } returns mockAliasIterator

        assertEquals(mockAliasIterator, t.getAliasesByOwner(accountId, from, to))
    }

    @Test
    fun addOrUpdateAlias_addAlias() {
        val transaction = mockk<Transaction>()
        every { transaction.senderId } returns 123L
        every { transaction.blockTimestamp } returns 34

        val attachment = mockk<MessagingAliasAssignment>()
        every { attachment.aliasURI } returns "aliasURI"
        every { attachment.aliasName } returns ""
        every { attachment.aliasName } returns ""

        t.addOrUpdateAlias(transaction, attachment)

        val savedAliasCaptor = CapturingSlot<Alias>()

        verify { aliasTableMock.insert(capture(savedAliasCaptor)) }

        val savedAlias = savedAliasCaptor.captured
        assertNotNull(savedAlias)

        assertEquals(transaction.senderId, savedAlias.accountId)
        assertEquals(transaction.blockTimestamp.toLong(), savedAlias.timestamp.toLong())
        assertEquals(attachment.aliasURI, savedAlias.aliasURI)
    }

    @Test
    fun addOrUpdateAlias_updateAlias() {
        val aliasName = "aliasName"
        val mockAlias = mockk<Alias>()

        every { aliasStoreMock.getAlias(eq(aliasName)) } returns mockAlias

        val transaction = mockk<Transaction>()
        every { transaction.senderId } returns 123L
        every { transaction.blockTimestamp } returns 34

        val attachment = mockk<MessagingAliasAssignment>()
        every { attachment.aliasName } returns aliasName
        every { attachment.aliasURI } returns "aliasURI"

        t.addOrUpdateAlias(transaction, attachment)

        verify { mockAlias.accountId = eq(transaction.senderId) }
        verify { mockAlias.timestamp = eq(transaction.blockTimestamp) }
        verify { mockAlias.aliasURI = eq(attachment.aliasURI) }

        verify { aliasTableMock.insert(eq(mockAlias)) }
    }

    @Test
    fun sellAlias_forBurst_newOffer() {
        val aliasName = "aliasName"
        val aliasId = 123L
        val mockAlias = mockk<Alias>()
        every { mockAlias.id } returns aliasId

        every { aliasStoreMock.getAlias(eq(aliasName)) } returns mockAlias

        val mockOfferKey = mockk<BurstKey>()
        every { offerDbKeyFactoryMock.newKey(eq(aliasId)) } returns mockOfferKey

        val pricePlanck = 500L

        val newOwnerId = 234L
        val timestamp = 567

        val transaction = mockk<Transaction>()
        val attachment = mockk<MessagingAliasSell>()
        every { attachment.aliasName } returns aliasName
        every { attachment.pricePlanck } returns pricePlanck
        every { transaction.blockTimestamp } returns timestamp
        every { transaction.recipientId } returns newOwnerId

        t.sellAlias(transaction, attachment)

        val mockOfferCaptor = CapturingSlot<Offer>()

        verify { offerTableMock.insert(capture(mockOfferCaptor)) }

        val savedOffer = mockOfferCaptor.captured
        assertEquals(newOwnerId, savedOffer.buyerId)
        assertEquals(pricePlanck, savedOffer.pricePlanck)
    }

    @Test
    fun sellAlias_forBurst_offerExists() {
        val aliasName = "aliasName"
        val aliasId = 123L
        val mockAlias = mockk<Alias>()
        every { mockAlias.id } returns aliasId

        every { aliasStoreMock.getAlias(eq(aliasName)) } returns mockAlias

        val mockOfferKey = mockk<BurstKey>()
        val mockOffer = mockk<Offer>()
        every { offerDbKeyFactoryMock.newKey(eq(aliasId)) } returns mockOfferKey
        every { offerTableMock[eq(mockOfferKey)] } returns mockOffer

        val pricePlanck = 500L

        val newOwnerId = 234L
        val timestamp = 567

        val transaction = mockk<Transaction>()
        val attachment = mockk<MessagingAliasSell>()
        every { attachment.aliasName } returns aliasName
        every { attachment.pricePlanck } returns pricePlanck
        every { transaction.blockTimestamp } returns timestamp
        every { transaction.recipientId } returns newOwnerId

        t.sellAlias(transaction, attachment)

        verify { mockOffer.pricePlanck = eq(pricePlanck) }
        verify { mockOffer.buyerId = eq(newOwnerId) }

        verify { offerTableMock.insert(eq(mockOffer)) }
    }

    @Test
    fun sellAlias_forFree() {
        val aliasName = "aliasName"
        val aliasId = 123L
        val mockAlias = mockk<Alias>()
        every { mockAlias.id } returns aliasId

        every { aliasStoreMock.getAlias(eq(aliasName)) } returns mockAlias

        val mockOfferKey = mockk<BurstKey>()
        val mockOffer = mockk<Offer>()
        every { offerDbKeyFactoryMock.newKey(eq(aliasId)) } returns mockOfferKey
        every { offerTableMock[eq(mockOfferKey)] } returns mockOffer

        val pricePlanck = 0L

        val newOwnerId = 234L
        val timestamp = 567

        val transaction = mockk<Transaction>()
        val attachment = mockk<MessagingAliasSell>()
        every { attachment.aliasName } returns aliasName
        every { attachment.pricePlanck } returns pricePlanck
        every { transaction.blockTimestamp } returns timestamp
        every { transaction.recipientId } returns newOwnerId

        t.sellAlias(transaction, attachment)

        verify { mockAlias.accountId = newOwnerId }
        verify { mockAlias.timestamp = eq(timestamp) }
        verify { aliasTableMock.insert(mockAlias) }

        verify { offerTableMock.delete(eq(mockOffer)) }
    }

    @Test
    fun changeOwner() {
        val aliasName = "aliasName"
        val aliasId = 123L
        val mockAlias = mockk<Alias>()
        every { mockAlias.id } returns aliasId

        every { aliasStoreMock.getAlias(eq(aliasName)) } returns mockAlias

        val mockOfferKey = mockk<BurstKey>()
        val mockOffer = mockk<Offer>()
        every { offerDbKeyFactoryMock.newKey(eq(aliasId)) } returns mockOfferKey
        every { offerTableMock[eq(mockOfferKey)] } returns mockOffer

        val newOwnerId = 234L
        val timestamp = 567

        t.changeOwner(newOwnerId, aliasName, timestamp)

        verify { mockAlias.accountId = newOwnerId }
        verify { mockAlias.timestamp = eq(timestamp) }
        verify { aliasTableMock.insert(mockAlias) }

        verify { offerTableMock.delete(eq(mockOffer)) }
    }
}
