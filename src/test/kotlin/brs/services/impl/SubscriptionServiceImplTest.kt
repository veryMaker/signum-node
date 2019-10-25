package brs.services.impl

import brs.services.BlockchainService
import brs.entity.Subscription
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.TransactionDb
import brs.db.VersionedEntityTable
import brs.db.store.SubscriptionStore
import brs.services.AccountService
import brs.services.AliasService
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SubscriptionServiceImplTest : AbstractUnitTest() {

    private lateinit var t: SubscriptionServiceImpl

    private lateinit var mockSubscriptionStore: SubscriptionStore
    private lateinit var mockSubscriptionTable: VersionedEntityTable<Subscription>
    private lateinit var mockSubscriptionDbKeyFactory: LongKeyFactory<Subscription>
    private lateinit var transactionDb: TransactionDb
    private lateinit var blockchainService: BlockchainService
    private lateinit var aliasService: AliasService
    private lateinit var accountService: AccountService


    @Before
    fun setUp() {
        mockSubscriptionStore = mock()
        mockSubscriptionTable = mock()
        mockSubscriptionDbKeyFactory = mock()
        transactionDb = mock()
        blockchainService = mock()
        aliasService = mock()
        accountService = mock()

        whenever(mockSubscriptionStore.subscriptionTable).doReturn(mockSubscriptionTable)
        whenever(mockSubscriptionStore.subscriptionDbKeyFactory).doReturn(mockSubscriptionDbKeyFactory)

        t = SubscriptionServiceImpl(QuickMocker.dependencyProvider(
            mockSubscriptionStore,
            transactionDb,
            blockchainService,
            aliasService,
            accountService
        ))
    }

    @Test
    fun getSubscription() {
        val subscriptionId = 123L

        val mockSubscriptionKey = mock<BurstKey>()

        val mockSubscription = mock<Subscription>()

        whenever(mockSubscriptionDbKeyFactory.newKey(eq(subscriptionId))).doReturn(mockSubscriptionKey)
        whenever(mockSubscriptionTable.get(eq(mockSubscriptionKey))).doReturn(mockSubscription)

        assertEquals(mockSubscription, t.getSubscription(subscriptionId))
    }

    @Test
    fun getSubscriptionsByParticipant() {
        val accountId = 123L

        val mockSubscriptionIterator = mockCollection<Subscription>()
        whenever(mockSubscriptionStore.getSubscriptionsByParticipant(eq(accountId))).doReturn(mockSubscriptionIterator)

        assertEquals(mockSubscriptionIterator, t.getSubscriptionsByParticipant(accountId))
    }

    @Test
    fun getSubscriptionsToId() {
        val accountId = 123L

        val mockSubscriptionIterator = mockCollection<Subscription>()
        whenever(mockSubscriptionStore.getSubscriptionsToId(eq(accountId))).doReturn(mockSubscriptionIterator)

        assertEquals(mockSubscriptionIterator, t.getSubscriptionsToId(accountId))
    }
}
