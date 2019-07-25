package brs.services.impl

import brs.Alias
import brs.Alias.Offer
import brs.Attachment.MessagingAliasAssignment
import brs.Attachment.MessagingAliasSell
import brs.Transaction
import brs.common.AbstractUnitTest
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.VersionedEntityTable
import brs.db.store.AliasStore
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class AliasServiceImplTest : AbstractUnitTest() {

    private var t: AliasServiceImpl? = null

    private var aliasStoreMock: AliasStore? = null
    private var aliasTableMock: VersionedEntityTable<Alias>? = null
    private var aliasDbKeyFactoryMock: BurstKey.LongKeyFactory<Alias>? = null
    private var offerTableMock: VersionedEntityTable<Offer>? = null
    private var offerDbKeyFactoryMock: BurstKey.LongKeyFactory<Offer>? = null

    @Before
    fun setUp() {
        aliasStoreMock = mock()
        aliasTableMock = mock()
        aliasDbKeyFactoryMock = mock()
        offerTableMock = mock()
        offerDbKeyFactoryMock = mock()

        whenever(aliasStoreMock!!.aliasTable).thenReturn(aliasTableMock)
        whenever(aliasStoreMock!!.aliasDbKeyFactory).thenReturn(aliasDbKeyFactoryMock)
        whenever(aliasStoreMock!!.offerTable).thenReturn(offerTableMock)
        whenever(aliasStoreMock!!.offerDbKeyFactory).thenReturn(offerDbKeyFactoryMock)

        t = AliasServiceImpl(aliasStoreMock!!)
    }

    @Test
    fun getAlias() {
        val aliasName = "aliasName"
        val mockAlias = mock<Alias>()

        whenever(aliasStoreMock!!.getAlias(eq(aliasName))).thenReturn(mockAlias)

        assertEquals(mockAlias, t!!.getAlias(aliasName))
    }

    @Test
    fun getAlias_byId() {
        val id = 123L
        val mockKey = mock<BurstKey>()
        val mockAlias = mock<Alias>()

        whenever(aliasDbKeyFactoryMock!!.newKey(eq(id))).thenReturn(mockKey)
        whenever(aliasTableMock!!.get(eq(mockKey))).thenReturn(mockAlias)

        assertEquals(mockAlias, t!!.getAlias(id))
    }

    @Test
    fun getOffer() {
        val aliasId = 123L
        val mockAlias = mock<Alias>()
        whenever(mockAlias.id).thenReturn(aliasId)
        val mockOfferKey = mock<BurstKey>()
        val mockOffer = mock<Offer>()

        whenever(offerDbKeyFactoryMock!!.newKey(eq<Long>(aliasId))).thenReturn(mockOfferKey)
        whenever(offerTableMock!!.get(eq(mockOfferKey))).thenReturn(mockOffer)

        assertEquals(mockOffer, t!!.getOffer(mockAlias))
    }

    @Test
    fun getAliasCount() {
        whenever(aliasTableMock!!.count).thenReturn(5)
        assertEquals(5L, t!!.aliasCount)
    }

    @Test
    fun getAliasesByOwner() {
        val accountId = 123L
        val from = 0
        val to = 1

        val mockAliasIterator = mockCollection<Alias>()

        whenever(aliasStoreMock!!.getAliasesByOwner(eq(accountId), eq(from), eq(to))).thenReturn(mockAliasIterator)

        assertEquals(mockAliasIterator, t!!.getAliasesByOwner(accountId, from, to))
    }

    @Test
    fun addOrUpdateAlias_addAlias() {
        val transaction = mock<Transaction>()
        whenever(transaction.senderId).thenReturn(123L)
        whenever(transaction.blockTimestamp).thenReturn(34)

        val attachment = mock<MessagingAliasAssignment>()
        whenever(attachment.aliasURI).thenReturn("aliasURI")

        t!!.addOrUpdateAlias(transaction, attachment)

        val savedAliasCaptor = argumentCaptor<Alias>()

        verify(aliasTableMock!!).insert(savedAliasCaptor.capture())

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

        whenever(aliasStoreMock!!.getAlias(eq(aliasName))).thenReturn(mockAlias)

        val transaction = mock<Transaction>()
        whenever(transaction.senderId).thenReturn(123L)
        whenever(transaction.blockTimestamp).thenReturn(34)

        val attachment = mock<MessagingAliasAssignment>()
        whenever(attachment.aliasName).thenReturn(aliasName)
        whenever(attachment.aliasURI).thenReturn("aliasURI")

        t!!.addOrUpdateAlias(transaction, attachment)

        verify(mockAlias).accountId = eq(transaction.senderId)
        verify(mockAlias).timestamp = eq(transaction.blockTimestamp)
        verify(mockAlias).aliasURI = eq(attachment.aliasURI)

        verify(aliasTableMock!!).insert(eq(mockAlias))
    }

    @Test
    fun sellAlias_forBurst_newOffer() {
        val aliasName = "aliasName"
        val aliasId = 123L
        val mockAlias = mock<Alias>()
        whenever(mockAlias.id).thenReturn(aliasId)

        whenever(aliasStoreMock!!.getAlias(eq(aliasName))).thenReturn(mockAlias)

        val mockOfferKey = mock<BurstKey>()
        whenever(offerDbKeyFactoryMock!!.newKey(eq(aliasId))).thenReturn(mockOfferKey)

        val priceNQT = 500L

        val newOwnerId = 234L
        val timestamp = 567

        val transaction = mock<Transaction>()
        val attachment = mock<MessagingAliasSell>()
        whenever(attachment.aliasName).thenReturn(aliasName)
        whenever(attachment.priceNQT).thenReturn(priceNQT)
        whenever(transaction.blockTimestamp).thenReturn(timestamp)
        whenever(transaction.recipientId).thenReturn(newOwnerId)

        t!!.sellAlias(transaction, attachment)

        val mockOfferCaptor = argumentCaptor<Offer>()

        verify(offerTableMock!!).insert(mockOfferCaptor.capture())

        val savedOffer = mockOfferCaptor.firstValue
        assertEquals(newOwnerId, savedOffer.buyerId)
        assertEquals(priceNQT, savedOffer.priceNQT)
    }

    @Test
    fun sellAlias_forBurst_offerExists() {
        val aliasName = "aliasName"
        val aliasId = 123L
        val mockAlias = mock<Alias>()
        whenever(mockAlias.id).thenReturn(aliasId)

        whenever(aliasStoreMock!!.getAlias(eq(aliasName))).thenReturn(mockAlias)

        val mockOfferKey = mock<BurstKey>()
        val mockOffer = mock<Offer>()
        whenever(offerDbKeyFactoryMock!!.newKey(eq(aliasId))).thenReturn(mockOfferKey)
        whenever(offerTableMock!!.get(eq(mockOfferKey))).thenReturn(mockOffer)

        val priceNQT = 500L

        val newOwnerId = 234L
        val timestamp = 567

        val transaction = mock<Transaction>()
        val attachment = mock<MessagingAliasSell>()
        whenever(attachment.aliasName).thenReturn(aliasName)
        whenever(attachment.priceNQT).thenReturn(priceNQT)
        whenever(transaction.blockTimestamp).thenReturn(timestamp)
        whenever(transaction.recipientId).thenReturn(newOwnerId)

        t!!.sellAlias(transaction, attachment)

        verify(mockOffer).priceNQT = eq(priceNQT)
        verify(mockOffer).buyerId = eq(newOwnerId)

        verify(offerTableMock!!).insert(eq(mockOffer))
    }

    @Test
    fun sellAlias_forFree() {
        val aliasName = "aliasName"
        val aliasId = 123L
        val mockAlias = mock<Alias>()
        whenever(mockAlias.id).thenReturn(aliasId)

        whenever(aliasStoreMock!!.getAlias(eq(aliasName))).thenReturn(mockAlias)

        val mockOfferKey = mock<BurstKey>()
        val mockOffer = mock<Offer>()
        whenever(offerDbKeyFactoryMock!!.newKey(eq(aliasId))).thenReturn(mockOfferKey)
        whenever(offerTableMock!!.get(eq(mockOfferKey))).thenReturn(mockOffer)

        val priceNQT = 0L

        val newOwnerId = 234L
        val timestamp = 567

        val transaction = mock<Transaction>()
        val attachment = mock<MessagingAliasSell>()
        whenever(attachment.aliasName).thenReturn(aliasName)
        whenever(attachment.priceNQT).thenReturn(priceNQT)
        whenever(transaction.blockTimestamp).thenReturn(timestamp)
        whenever(transaction.recipientId).thenReturn(newOwnerId)

        t!!.sellAlias(transaction, attachment)

        verify(mockAlias).accountId = newOwnerId
        verify(mockAlias).timestamp = eq(timestamp)
        verify(aliasTableMock!!).insert(mockAlias)

        verify(offerTableMock!!).delete(eq(mockOffer))
    }

    @Test
    fun changeOwner() {
        val aliasName = "aliasName"
        val aliasId = 123L
        val mockAlias = mock<Alias>()
        whenever(mockAlias.id).thenReturn(aliasId)

        whenever(aliasStoreMock!!.getAlias(eq(aliasName))).thenReturn(mockAlias)

        val mockOfferKey = mock<BurstKey>()
        val mockOffer = mock<Offer>()
        whenever(offerDbKeyFactoryMock!!.newKey(eq(aliasId))).thenReturn(mockOfferKey)
        whenever(offerTableMock!!.get(eq(mockOfferKey))).thenReturn(mockOffer)

        val newOwnerId = 234L
        val timestamp = 567

        t!!.changeOwner(newOwnerId, aliasName, timestamp)

        verify(mockAlias).accountId = newOwnerId
        verify(mockAlias).timestamp = eq(timestamp)
        verify(aliasTableMock!!).insert(mockAlias)

        verify(offerTableMock!!).delete(eq(mockOffer))
    }
}
