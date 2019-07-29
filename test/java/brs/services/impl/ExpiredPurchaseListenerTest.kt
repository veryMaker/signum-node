package brs.services.impl

import brs.Account
import brs.Block
import brs.DigitalGoodsStore.Purchase
import brs.common.AbstractUnitTest
import brs.services.AccountService
import brs.services.DGSGoodsStoreService
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test


class ExpiredPurchaseListenerTest : AbstractUnitTest() {

    private var accountServiceMock: AccountService? = null
    private var dgsGoodsStoreServiceMock: DGSGoodsStoreService? = null

    private var t: DGSGoodsStoreServiceImpl.ExpiredPurchaseListener? = null

    @Before
    fun setUp() {
        accountServiceMock = mock<AccountService>()
        dgsGoodsStoreServiceMock = mock<DGSGoodsStoreService>()

        t = DGSGoodsStoreServiceImpl.ExpiredPurchaseListener(accountServiceMock, dgsGoodsStoreServiceMock)
    }

    @Test
    fun notify_processesExpiredPurchases() {
        val blockTimestamp = 123
        val block = mock<Block>()
        whenever(block.timestamp).doReturn(blockTimestamp)

        val purchaseBuyerId: Long = 34
        val purchaseBuyer = mock<Account>()
        whenever(purchaseBuyer.getId()).doReturn(purchaseBuyerId)
        whenever(accountServiceMock!!.getAccount(eq(purchaseBuyer.getId()))).doReturn(purchaseBuyer)

        val expiredPurchase = mock<Purchase>()
        whenever(expiredPurchase.quantity).doReturn(5)
        whenever(expiredPurchase.priceNQT).doReturn(3000L)
        whenever(expiredPurchase.buyerId).doReturn(purchaseBuyerId)

        val mockIterator = mockCollection(expiredPurchase)
        whenever(dgsGoodsStoreServiceMock!!.getExpiredPendingPurchases(eq(blockTimestamp))).doReturn(mockIterator)

        t!!.accept(block)

        verify(accountServiceMock!!).addToUnconfirmedBalanceNQT(eq(purchaseBuyer), eq(15000L))

        verify(dgsGoodsStoreServiceMock!!).setPending(eq(expiredPurchase), eq(false))
    }
}