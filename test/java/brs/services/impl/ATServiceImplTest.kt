package brs.services.impl

import brs.at.AT
import brs.db.store.ATStore
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test

import org.junit.Assert.assertEquals

class ATServiceImplTest {

    private var t: ATServiceImpl? = null

    private var mockATStore: ATStore? = null

    @Before
    fun setUp() {
        mockATStore = mock()

        t = ATServiceImpl(mockATStore)
    }

    @Test
    fun getAllATIds() {
        val mockATCollection = mock<Collection<Long>>()

        whenever(mockATStore!!.allATIds).doReturn(mockATCollection)

        assertEquals(mockATCollection, t!!.allATIds)
    }

    @Test
    fun getATsIssuedBy() {
        val accountId = 1L

        val mockATsIssuedByAccount = mock<List<Long>>()

        whenever(mockATStore!!.getATsIssuedBy(eq(accountId))).doReturn(mockATsIssuedByAccount)

        assertEquals(mockATsIssuedByAccount, t!!.getATsIssuedBy(accountId))
    }

    @Test
    fun getAT() {
        val atId = 123L

        val mockAT = mock<AT>()

        whenever(mockATStore!!.getAT(eq(atId))).doReturn(mockAT)

        assertEquals(mockAT, t!!.getAT(atId))
    }

}
