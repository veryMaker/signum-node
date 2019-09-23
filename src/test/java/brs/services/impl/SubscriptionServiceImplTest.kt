package brs.services.impl

import brs.Blockchain
import brs.Subscription
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

    private var t: SubscriptionServiceImpl? = null

    private var mockSubscriptionStore: SubscriptionStore? = null
    private var mockSubscriptionTable: VersionedEntityTable<Subscription>? = null
    private var mockSubscriptionDbKeyFactory: LongKeyFactory<Subscription>? = null
    private val transactionDb: TransactionDb? = null
    private val blockchain: Blockchain? = null
    private val aliasService: AliasService? = null
    private val accountService: AccountService? = null


    @Before
    fun setUp() {
        mockSubscriptionStore = mock()
        mockSubscriptionTable = mock()
        mockSubscriptionDbKeyFactory = mock()

        whenever(mockSubscriptionStore!!.subscriptionTable).doReturn(mockSubscriptionTable!!)
        whenever(mockSubscriptionStore!!.subscriptionDbKeyFactory).doReturn(mockSubscriptionDbKeyFactory!!)

        t = SubscriptionServiceImpl(QuickMocker.dependencyProvider(mockSubscriptionStore!!, transactionDb!!, blockchain!!, aliasService!!, accountService!!))
    }

    @Test
    fun getSubscription() {
        val subscriptionId = 123L

        val mockSubscriptionKey = mock<BurstKey>()

        val mockSubscription = mock<Subscription>()

        whenever(mockSubscriptionDbKeyFactory!!.newKey(eq(subscriptionId))).doReturn(mockSubscriptionKey)
        whenever(mockSubscriptionTable!!.get(eq(mockSubscriptionKey))).doReturn(mockSubscription)

        assertEquals(mockSubscription, t!!.getSubscription(subscriptionId))
    }

    @Test
    fun getSubscriptionsByParticipant() {
        val accountId = 123L

        val mockSubscriptionIterator = mockCollection<Subscription>()
        whenever(mockSubscriptionStore!!.getSubscriptionsByParticipant(eq(accountId))).doReturn(mockSubscriptionIterator)

        assertEquals(mockSubscriptionIterator, t!!.getSubscriptionsByParticipant(accountId))
    }

    @Test
    fun getSubscriptionsToId() {
        val accountId = 123L

        val mockSubscriptionIterator = mockCollection<Subscription>()
        whenever(mockSubscriptionStore!!.getSubscriptionsToId(eq(accountId))).doReturn(mockSubscriptionIterator)

        assertEquals(mockSubscriptionIterator, t!!.getSubscriptionsToId(accountId))
    }
}
