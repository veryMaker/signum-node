package brs.services.impl

import brs.services.BlockchainService
import brs.entity.Subscription
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.TransactionDb
import brs.db.VersionedEntityTable
import brs.db.SubscriptionStore
import brs.services.AccountService
import brs.services.AliasService
import io.mockk.mockk
import io.mockk.every
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
        mockSubscriptionStore = mockk()
        mockSubscriptionTable = mockk()
        mockSubscriptionDbKeyFactory = mockk()
        transactionDb = mockk()
        blockchainService = mockk()
        aliasService = mockk()
        accountService = mockk()

        every { mockSubscriptionStore.subscriptionTable } returns mockSubscriptionTable
        every { mockSubscriptionStore.subscriptionDbKeyFactory } returns mockSubscriptionDbKeyFactory

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

        val mockSubscriptionKey = mockk<BurstKey>()

        val mockSubscription = mockk<Subscription>()

        every { mockSubscriptionDbKeyFactory.newKey(eq(subscriptionId)) } returns mockSubscriptionKey
        every { mockSubscriptionTable.get(eq(mockSubscriptionKey)) } returns mockSubscription

        assertEquals(mockSubscription, t.getSubscription(subscriptionId))
    }

    @Test
    fun getSubscriptionsByParticipant() {
        val accountId = 123L

        val mockSubscriptionIterator = mockCollection<Subscription>()
        every { mockSubscriptionStore.getSubscriptionsByParticipant(eq(accountId)) } returns mockSubscriptionIterator

        assertEquals(mockSubscriptionIterator, t.getSubscriptionsByParticipant(accountId))
    }

    @Test
    fun getSubscriptionsToId() {
        val accountId = 123L

        val mockSubscriptionIterator = mockCollection<Subscription>()
        every { mockSubscriptionStore.getSubscriptionsToId(eq(accountId)) } returns mockSubscriptionIterator

        assertEquals(mockSubscriptionIterator, t.getSubscriptionsToId(accountId))
    }
}
