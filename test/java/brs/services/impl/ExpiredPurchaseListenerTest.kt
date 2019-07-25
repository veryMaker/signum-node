package brs.services.impl

import brs.Account
import brs.Block
import brs.DigitalGoodsStore.Purchase
import brs.common.AbstractUnitTest
import brs.services.AccountService
import brs.services.DGSGoodsStoreService
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
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
        whenever(block.timestamp).thenReturn(blockTimestamp)

        val purchaseBuyerId: Long = 34
        val purchaseBuyer = mock<Account>()
        whenever(purchaseBuyer.getId()).thenReturn(purchaseBuyerId)
        whenever(accountServiceMock!!.getAccount(eq(purchaseBuyer.getId()))).thenReturn(purchaseBuyer)

        val expiredPurchase = mock<Purchase>()
        whenever(expiredPurchase.quantity).thenReturn(5)
        whenever(expiredPurchase.priceNQT).thenReturn(3000L)
        whenever(expiredPurchase.buyerId).thenReturn(purchaseBuyerId)

        val mockIterator = mockCollection(expiredPurchase)
        whenever(dgsGoodsStoreServiceMock!!.getExpiredPendingPurchases(eq(blockTimestamp))).thenReturn(mockIterator)

        t!!.accept(block)

        verify(accountServiceMock!!).addToUnconfirmedBalanceNQT(eq(purchaseBuyer), eq(15000L))

        verify(dgsGoodsStoreServiceMock!!).setPending(eq(expiredPurchase), eq(false))
    }
}