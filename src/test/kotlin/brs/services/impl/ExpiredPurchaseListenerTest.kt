package brs.services.impl

import brs.entity.Account
import brs.entity.Block
import brs.entity.DigitalGoodsStore.Purchase
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.services.AccountService
import brs.services.DigitalGoodsStoreService
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test


class ExpiredPurchaseListenerTest : AbstractUnitTest() {

    private lateinit var accountServiceMock: AccountService
    private lateinit var digitalGoodsStoreServiceMock: DigitalGoodsStoreService

    private lateinit var t: (Block) -> Unit

    @Before
    fun setUp() {
        accountServiceMock = mock()
        digitalGoodsStoreServiceMock = mock()

        t = DigitalGoodsStoreServiceImpl.expiredPurchaseListener(QuickMocker.dependencyProvider(
            accountServiceMock,
            digitalGoodsStoreServiceMock
        ))
    }

    @Test
    fun notify_processesExpiredPurchases() {
        val blockTimestamp = 123
        val block = mock<Block>()
        whenever(block.timestamp).doReturn(blockTimestamp)

        val purchaseBuyerId: Long = 34
        val purchaseBuyer = mock<Account>()
        whenever(purchaseBuyer.id).doReturn(purchaseBuyerId)
        whenever(accountServiceMock.getAccount(eq(purchaseBuyer.id))).doReturn(purchaseBuyer)

        val expiredPurchase = mock<Purchase>()
        whenever(expiredPurchase.quantity).doReturn(5)
        whenever(expiredPurchase.pricePlanck).doReturn(3000L)
        whenever(expiredPurchase.buyerId).doReturn(purchaseBuyerId)

        val mockIterator = mockCollection(expiredPurchase)
        whenever(digitalGoodsStoreServiceMock.getExpiredPendingPurchases(eq(blockTimestamp))).doReturn(mockIterator)

        t(block)

        verify(accountServiceMock).addToUnconfirmedBalancePlanck(eq(purchaseBuyer), eq(15000L))

        verify(digitalGoodsStoreServiceMock).setPending(eq(expiredPurchase), eq(false))
    }
}