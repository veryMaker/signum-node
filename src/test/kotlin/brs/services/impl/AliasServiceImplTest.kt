package brs.services.impl

import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.db.AliasStore
import brs.db.BurstKey
import brs.db.MutableEntityTable
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
    private lateinit var aliasTableMock: MutableEntityTable<Alias>
    private lateinit var aliasDbKeyFactoryMock: BurstKey.LongKeyFactory<Alias>
    private lateinit var offerTableMock: MutableEntityTable<Offer>
    private lateinit var offerDbKeyFactoryMock: BurstKey.LongKeyFactory<Offer>

    @Before
    fun setUp() {
        aliasStoreMock = mockk(relaxed = true)
        aliasTableMock = mockk(relaxed = true)
        aliasDbKeyFactoryMock = mockk(relaxed = true)
        offerTableMock = mockk(relaxed = true)
        offerDbKeyFactoryMock = mockk(relaxed = true)

        every { aliasStoreMock.getAlias(any()) } returns null
        every { aliasStoreMock.aliasTable } returns aliasTableMock
        every { aliasStoreMock.aliasDbKeyFactory } returns aliasDbKeyFactoryMock
        every { aliasStoreMock.offerTable } returns offerTableMock
        every { aliasStoreMock.offerDbKeyFactory } returns offerDbKeyFactoryMock
        every { offerTableMock[any()] } returns null
        every { aliasDbKeyFactoryMock.newKey(any<Long>()) } returns mockk(relaxed = true)

        t = AliasServiceImpl(QuickMocker.dependencyProvider(aliasStoreMock))
    }

    @Test
    fun getAlias() {
        val aliasName = "aliasName"
        val mockAlias = mockk<Alias>(relaxed = true)

        every { aliasStoreMock.getAlias(eq(aliasName)) } returns mockAlias

        assertEquals(mockAlias, t.getAlias(aliasName))
    }

    @Test
    fun getAlias_byId() {
        val id = 123L
        val mockKey = mockk<BurstKey>(relaxed = true)
        val mockAlias = mockk<Alias>(relaxed = true)

        every { aliasDbKeyFactoryMock.newKey(eq(id)) } returns mockKey
        every { aliasTableMock[eq(mockKey)] } returns mockAlias

        assertEquals(mockAlias, t.getAlias(id))
    }

    @Test
    fun getOffer() {
        val aliasId = 123L
        val mockAlias = mockk<Alias>(relaxed = true)
        every { mockAlias.id } returns aliasId
        val mockOfferKey = mockk<BurstKey>(relaxed = true)
        val mockOffer = mockk<Offer>(relaxed = true)

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
        val transaction = mockk<Transaction>(relaxed = true)
        every { transaction.senderId } returns 123L
        every { transaction.blockTimestamp } returns 34

        val attachment = mockk<MessagingAliasAssignment>(relaxed = true)
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
        val mockAlias = mockk<Alias>(relaxed = true)

        every { aliasStoreMock.getAlias(eq(aliasName)) } returns mockAlias

        val transaction = mockk<Transaction>(relaxed = true)
        val senderId = 123L
        every { transaction.senderId } returns senderId
        val blockTimestamp = 34
        every { transaction.blockTimestamp } returns blockTimestamp

        val attachment = mockk<MessagingAliasAssignment>(relaxed = true)
        every { attachment.aliasName } returns aliasName
        val aliasURI = "aliasURI"
        every { attachment.aliasURI } returns aliasURI

        t.addOrUpdateAlias(transaction, attachment)

        verify { mockAlias.accountId = eq(senderId) }
        verify { mockAlias.timestamp = eq(blockTimestamp) }
        verify { mockAlias.aliasURI = eq(aliasURI) }

        verify { aliasTableMock.insert(eq(mockAlias)) }
    }

    @Test
    fun sellAlias_forBurst_newOffer() {
        val aliasName = "aliasName"
        val aliasId = 123L
        val mockAlias = mockk<Alias>(relaxed = true)
        every { mockAlias.id } returns aliasId

        every { aliasStoreMock.getAlias(eq(aliasName)) } returns mockAlias

        val mockOfferKey = mockk<BurstKey>(relaxed = true)
        every { offerDbKeyFactoryMock.newKey(eq(aliasId)) } returns mockOfferKey

        val pricePlanck = 500L

        val newOwnerId = 234L
        val timestamp = 567

        val transaction = mockk<Transaction>(relaxed = true)
        val attachment = mockk<MessagingAliasSell>(relaxed = true)
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
        val mockAlias = mockk<Alias>(relaxed = true)
        every { mockAlias.id } returns aliasId

        every { aliasStoreMock.getAlias(eq(aliasName)) } returns mockAlias

        val mockOfferKey = mockk<BurstKey>(relaxed = true)
        val mockOffer = mockk<Offer>(relaxed = true)
        every { offerDbKeyFactoryMock.newKey(eq(aliasId)) } returns mockOfferKey
        every { offerTableMock[eq(mockOfferKey)] } returns mockOffer

        val pricePlanck = 500L

        val newOwnerId = 234L
        val timestamp = 567

        val transaction = mockk<Transaction>(relaxed = true)
        val attachment = mockk<MessagingAliasSell>(relaxed = true)
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
        val mockAlias = mockk<Alias>(relaxed = true)
        every { mockAlias.id } returns aliasId

        every { aliasStoreMock.getAlias(eq(aliasName)) } returns mockAlias

        val mockOfferKey = mockk<BurstKey>(relaxed = true)
        val mockOffer = mockk<Offer>(relaxed = true)
        every { offerDbKeyFactoryMock.newKey(eq(aliasId)) } returns mockOfferKey
        every { offerTableMock[eq(mockOfferKey)] } returns mockOffer

        val pricePlanck = 0L

        val newOwnerId = 234L
        val timestamp = 567

        val transaction = mockk<Transaction>(relaxed = true)
        val attachment = mockk<MessagingAliasSell>(relaxed = true)
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
        val mockAlias = mockk<Alias>(relaxed = true)
        every { mockAlias.id } returns aliasId

        every { aliasStoreMock.getAlias(eq(aliasName)) } returns mockAlias

        val mockOfferKey = mockk<BurstKey>(relaxed = true)
        val mockOffer = mockk<Offer>(relaxed = true)
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
