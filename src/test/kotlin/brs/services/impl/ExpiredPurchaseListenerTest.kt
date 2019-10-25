package brs.services.impl

import brs.Account
import brs.Block
import brs.DigitalGoodsStore.Purchase
import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.services.AccountService
import brs.services.DGSGoodsStoreService
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test


class ExpiredPurchaseListenerTest : AbstractUnitTest() {

    private lateinit var accountServiceMock: AccountService
    private lateinit var dgsGoodsStoreServiceMock: DGSGoodsStoreService

    private lateinit var t: (Block) -> Unit

    @Before
    fun setUp() {
        accountServiceMock = mock()
        dgsGoodsStoreServiceMock = mock()

        t = DGSGoodsStoreServiceImpl.expiredPurchaseListener(QuickMocker.dependencyProvider(
            accountServiceMock,
            dgsGoodsStoreServiceMock
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
        whenever(dgsGoodsStoreServiceMock.getExpiredPendingPurchases(eq(blockTimestamp))).doReturn(mockIterator)

        t(block)

        verify(accountServiceMock).addToUnconfirmedBalancePlanck(eq(purchaseBuyer), eq(15000L))

        verify(dgsGoodsStoreServiceMock).setPending(eq(expiredPurchase), eq(false))
    }
}