package brs.services.impl

import brs.at.AT
import brs.common.QuickMocker
import brs.db.ATStore
import io.mockk.mockk
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ATServiceImplTest {

    private lateinit var t: ATServiceImpl

    private lateinit var mockATStore: ATStore

    @Before
    fun setUp() {
        mockATStore = mockk(relaxed = true)

        t = ATServiceImpl(QuickMocker.dependencyProvider(mockATStore))
    }

    @Test
    fun getAllATIds() {
        val mockATCollection = mockk<Collection<Long>>()

        every { mockATStore.getAllATIds() } returns mockATCollection

        assertEquals(mockATCollection, t.getAllATIds())
    }

    @Test
    fun getATsIssuedBy() {
        val accountId = 1L

        val mockATsIssuedByAccount = mockk<List<Long>>()

        every { mockATStore.getATsIssuedBy(eq(accountId)) } returns mockATsIssuedByAccount

        assertEquals(mockATsIssuedByAccount, t.getATsIssuedBy(accountId))
    }

    @Test
    fun getAT() {
        val atId = 123L

        val mockAT = mockk<AT>(relaxed = true)

        every { mockATStore.getAT(eq(atId)) } returns mockAT

        assertEquals(mockAT, t.getAT(atId))
    }

}
