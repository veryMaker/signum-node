package brs.at

import brs.DependencyProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class ATTest {
    private lateinit var dp: DependencyProvider

    @Before
    fun setUp() {
        dp = AtTestHelper.setupMocks()
    }

    @Test
    fun testAddAt() {
        AtTestHelper.clearAddedAts(dp)
        val helloWorldReceived = AtomicBoolean(false)
        AtTestHelper.setOnAtAdded { at ->
            assertEquals("HelloWorld", at.name)
            helloWorldReceived.set(true)
        }
        AtTestHelper.addHelloWorldAT(dp)
        assertTrue(helloWorldReceived.get())

        val echoReceived = AtomicBoolean(false)
        AtTestHelper.setOnAtAdded { at ->
            assertEquals("Echo", at.name)
            echoReceived.set(true)
        }
        AtTestHelper.addEchoAT(dp)
        assertTrue(echoReceived.get())

        val tipThanksReceived = AtomicBoolean(false)
        AtTestHelper.setOnAtAdded { at ->
            assertEquals("TipThanks", at.name)
            tipThanksReceived.set(true)
        }
        AtTestHelper.addTipThanksAT(dp)
        assertTrue(tipThanksReceived.get())
        assertEquals(3, AT.getOrderedATs(dp).size.toLong())
    }
}
