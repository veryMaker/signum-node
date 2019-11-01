package brs.services.impl

import brs.entity.Alias
import brs.entity.Alias.Offer
import brs.transaction.appendix.Attachment.MessagingAliasAssignment
import brs.transaction.appendix.Attachment.MessagingAliasSell
import brs.entity.Transaction
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.db.BurstKey
import brs.db.VersionedEntityTable
import brs.db.AliasStore
import com.nhaarman.mockitokotlin2.*
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
        aliasStoreMock = mock()
        aliasTableMock = mock()
        aliasDbKeyFactoryMock = mock()
        offerTableMock = mock()
        offerDbKeyFactoryMock = mock()

        whenever(aliasStoreMock.aliasTable).doReturn(aliasTableMock)
        whenever(aliasStoreMock.aliasDbKeyFactory).doReturn(aliasDbKeyFactoryMock)
        whenever(aliasStoreMock.offerTable).doReturn(offerTableMock)
        whenever(aliasStoreMock.offerDbKeyFactory).doReturn(offerDbKeyFactoryMock)
        whenever(aliasDbKeyFactoryMock.newKey(any<Long>())).doReturn(mock())

        t = AliasServiceImpl(QuickMocker.dependencyProvider(aliasStoreMock))
    }

    @Test
    fun getAlias() {
        val aliasName = "aliasName"
        val mockAlias = mock<Alias>()

        whenever(aliasStoreMock.getAlias(eq(aliasName))).doReturn(mockAlias)

        assertEquals(mockAlias, t.getAlias(aliasName))
    }

    @Test
    fun getAlias_byId() {
        val id = 123L
        val mockKey = mock<BurstKey>()
        val mockAlias = mock<Alias>()

        whenever(aliasDbKeyFactoryMock.newKey(eq(id))).doReturn(mockKey)
        whenever(aliasTableMock[eq(mockKey)]).doReturn(mockAlias)

        assertEquals(mockAlias, t.getAlias(id))
    }

    @Test
    fun getOffer() {
        val aliasId = 123L
        val mockAlias = mock<Alias>()
        whenever(mockAlias.id).doReturn(aliasId)
        val mockOfferKey = mock<BurstKey>()
        val mockOffer = mock<Offer>()

        whenever(offerDbKeyFactoryMock.newKey(eq(aliasId))).doReturn(mockOfferKey)
        whenever(offerTableMock[eq(mockOfferKey)]).doReturn(mockOffer)

        assertEquals(mockOffer, t.getOffer(mockAlias))
    }

    @Test
    fun getAliasCount() {
        whenever(aliasTableMock.count).doReturn(5)
        assertEquals(5L, t.getAliasCount())
    }

    @Test
    fun getAliasesByOwner() {
        val accountId = 123L
        val from = 0
        val to = 1

        val mockAliasIterator = mockCollection<Alias>()

        whenever(aliasStoreMock.getAliasesByOwner(eq(accountId), eq(from), eq(to))).doReturn(mockAliasIterator)

        assertEquals(mockAliasIterator, t.getAliasesByOwner(accountId, from, to))
    }

    @Test
    fun addOrUpdateAlias_addAlias() {
        val transaction = mock<Transaction>()
        whenever(transaction.senderId).doReturn(123L)
        whenever(transaction.blockTimestamp).doReturn(34)

        val attachment = mock<MessagingAliasAssignment>()
        whenever(attachment.aliasURI).doReturn("aliasURI")
        whenever(attachment.aliasName).doReturn("")
        whenever(attachment.aliasName).doReturn("")

        t.addOrUpdateAlias(transaction, attachment)

        val savedAliasCaptor = argumentCaptor<Alias>()

        verify(aliasTableMock).insert(savedAliasCaptor.capture())

        val savedAlias = savedAliasCaptor.firstValue
        assertNotNull(savedAlias)

        assertEquals(transaction.senderId, savedAlias.accountId)
        assertEquals(transaction.blockTimestamp.toLong(), savedAlias.timestamp.toLong())
        assertEquals(attachment.aliasURI, savedAlias.aliasURI)
    }

    @Test
    fun addOrUpdateAlias_updateAlias() {
        val aliasName = "aliasName"
        val mockAlias = mock<Alias>()

        whenever(aliasStoreMock.getAlias(eq(aliasName))).doReturn(mockAlias)

        val transaction = mock<Transaction>()
        whenever(transaction.senderId).doReturn(123L)
        whenever(transaction.blockTimestamp).doReturn(34)

        val attachment = mock<MessagingAliasAssignment>()
        whenever(attachment.aliasName).doReturn(aliasName)
        whenever(attachment.aliasURI).doReturn("aliasURI")

        t.addOrUpdateAlias(transaction, attachment)

        verify(mockAlias).accountId = eq(transaction.senderId)
        verify(mockAlias).timestamp = eq(transaction.blockTimestamp)
        verify(mockAlias).aliasURI = eq(attachment.aliasURI)

        verify(aliasTableMock).insert(eq(mockAlias))
    }

    @Test
    fun sellAlias_forBurst_newOffer() {
        val aliasName = "aliasName"
        val aliasId = 123L
        val mockAlias = mock<Alias>()
        whenever(mockAlias.id).doReturn(aliasId)

        whenever(aliasStoreMock.getAlias(eq(aliasName))).doReturn(mockAlias)

        val mockOfferKey = mock<BurstKey>()
        whenever(offerDbKeyFactoryMock.newKey(eq(aliasId))).doReturn(mockOfferKey)

        val pricePlanck = 500L

        val newOwnerId = 234L
        val timestamp = 567

        val transaction = mock<Transaction>()
        val attachment = mock<MessagingAliasSell>()
        whenever(attachment.aliasName).doReturn(aliasName)
        whenever(attachment.pricePlanck).doReturn(pricePlanck)
        whenever(transaction.blockTimestamp).doReturn(timestamp)
        whenever(transaction.recipientId).doReturn(newOwnerId)

        t.sellAlias(transaction, attachment)

        val mockOfferCaptor = argumentCaptor<Offer>()

        verify(offerTableMock).insert(mockOfferCaptor.capture())

        val savedOffer = mockOfferCaptor.firstValue
        assertEquals(newOwnerId, savedOffer.buyerId)
        assertEquals(pricePlanck, savedOffer.pricePlanck)
    }

    @Test
    fun sellAlias_forBurst_offerExists() {
        val aliasName = "aliasName"
        val aliasId = 123L
        val mockAlias = mock<Alias>()
        whenever(mockAlias.id).doReturn(aliasId)

        whenever(aliasStoreMock.getAlias(eq(aliasName))).doReturn(mockAlias)

        val mockOfferKey = mock<BurstKey>()
        val mockOffer = mock<Offer>()
        whenever(offerDbKeyFactoryMock.newKey(eq(aliasId))).doReturn(mockOfferKey)
        whenever(offerTableMock[eq(mockOfferKey)]).doReturn(mockOffer)

        val pricePlanck = 500L

        val newOwnerId = 234L
        val timestamp = 567

        val transaction = mock<Transaction>()
        val attachment = mock<MessagingAliasSell>()
        whenever(attachment.aliasName).doReturn(aliasName)
        whenever(attachment.pricePlanck).doReturn(pricePlanck)
        whenever(transaction.blockTimestamp).doReturn(timestamp)
        whenever(transaction.recipientId).doReturn(newOwnerId)

        t.sellAlias(transaction, attachment)

        verify(mockOffer).pricePlanck = eq(pricePlanck)
        verify(mockOffer).buyerId = eq(newOwnerId)

        verify(offerTableMock).insert(eq(mockOffer))
    }

    @Test
    fun sellAlias_forFree() {
        val aliasName = "aliasName"
        val aliasId = 123L
        val mockAlias = mock<Alias>()
        whenever(mockAlias.id).doReturn(aliasId)

        whenever(aliasStoreMock.getAlias(eq(aliasName))).doReturn(mockAlias)

        val mockOfferKey = mock<BurstKey>()
        val mockOffer = mock<Offer>()
        whenever(offerDbKeyFactoryMock.newKey(eq(aliasId))).doReturn(mockOfferKey)
        whenever(offerTableMock[eq(mockOfferKey)]).doReturn(mockOffer)

        val pricePlanck = 0L

        val newOwnerId = 234L
        val timestamp = 567

        val transaction = mock<Transaction>()
        val attachment = mock<MessagingAliasSell>()
        whenever(attachment.aliasName).doReturn(aliasName)
        whenever(attachment.pricePlanck).doReturn(pricePlanck)
        whenever(transaction.blockTimestamp).doReturn(timestamp)
        whenever(transaction.recipientId).doReturn(newOwnerId)

        t.sellAlias(transaction, attachment)

        verify(mockAlias).accountId = newOwnerId
        verify(mockAlias).timestamp = eq(timestamp)
        verify(aliasTableMock).insert(mockAlias)

        verify(offerTableMock).delete(eq(mockOffer))
    }

    @Test
    fun changeOwner() {
        val aliasName = "aliasName"
        val aliasId = 123L
        val mockAlias = mock<Alias>()
        whenever(mockAlias.id).doReturn(aliasId)

        whenever(aliasStoreMock.getAlias(eq(aliasName))).doReturn(mockAlias)

        val mockOfferKey = mock<BurstKey>()
        val mockOffer = mock<Offer>()
        whenever(offerDbKeyFactoryMock.newKey(eq(aliasId))).doReturn(mockOfferKey)
        whenever(offerTableMock[eq(mockOfferKey)]).doReturn(mockOffer)

        val newOwnerId = 234L
        val timestamp = 567

        t.changeOwner(newOwnerId, aliasName, timestamp)

        verify(mockAlias).accountId = newOwnerId
        verify(mockAlias).timestamp = eq(timestamp)
        verify(aliasTableMock).insert(mockAlias)

        verify(offerTableMock).delete(eq(mockOffer))
    }
}
