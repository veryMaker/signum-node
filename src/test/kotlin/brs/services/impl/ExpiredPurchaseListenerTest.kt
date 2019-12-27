package brs.services.impl

import brs.common.AbstractUnitTest
import brs.common.QuickMocker
import brs.entity.Account
import brs.entity.Block
import brs.entity.Purchase
import brs.services.AccountService
import brs.services.DigitalGoodsStoreService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test


class ExpiredPurchaseListenerTest : AbstractUnitTest() {

    private lateinit var accountServiceMock: AccountService
    private lateinit var digitalGoodsStoreServiceMock: DigitalGoodsStoreService

    private lateinit var t: (Block) -> Unit

    @Before
    fun setUp() {
        accountServiceMock = mockk()
        digitalGoodsStoreServiceMock = mockk()

        t = DigitalGoodsStoreServiceImpl.expiredPurchaseListener(QuickMocker.dependencyProvider(
            accountServiceMock,
            digitalGoodsStoreServiceMock
        ))
    }

    @Test
    fun notify_processesExpiredPurchases() {
        val blockTimestamp = 123
        val block = mockk<Block>()
        every { block.timestamp } returns blockTimestamp

        val purchaseBuyerId: Long = 34
        val purchaseBuyer = mockk<Account>()
        every { purchaseBuyer.id } returns purchaseBuyerId
        every { accountServiceMock.getAccount(eq(purchaseBuyer.id)) } returns purchaseBuyer

        val expiredPurchase = mockk<Purchase>()
        every { expiredPurchase.quantity } returns 5
        every { expiredPurchase.pricePlanck } returns 3000L
        every { expiredPurchase.buyerId } returns purchaseBuyerId

        val mockIterator = mockCollection(expiredPurchase)
        every { digitalGoodsStoreServiceMock.getExpiredPendingPurchases(eq(blockTimestamp)) } returns mockIterator

        t(block)

        verify { accountServiceMock.addToUnconfirmedBalancePlanck(eq(purchaseBuyer), eq(15000L)) }

        verify { digitalGoodsStoreServiceMock.setPending(eq(expiredPurchase), eq(false)) }
    }
}