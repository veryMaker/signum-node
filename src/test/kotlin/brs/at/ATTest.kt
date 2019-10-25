package brs.at

import brs.entity.DependencyProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class ATTest {
    private lateinit var atTestHelper: AtTestHelper
    private lateinit var dp: DependencyProvider

    @Before
    fun setUp() {
        atTestHelper = AtTestHelper()
        dp = atTestHelper.setupMocks()
    }

    @Test
    fun testAddAt() {
        atTestHelper.clearAddedAts(dp)
        val helloWorldReceived = AtomicBoolean(false)
        atTestHelper.setOnAtAdded { at ->
            assertEquals("HelloWorld", at.name)
            helloWorldReceived.set(true)
        }
        atTestHelper.addHelloWorldAT(dp)
        assertTrue(helloWorldReceived.get())

        val echoReceived = AtomicBoolean(false)
        atTestHelper.setOnAtAdded { at ->
            assertEquals("Echo", at.name)
            echoReceived.set(true)
        }
        atTestHelper.addEchoAT(dp)
        assertTrue(echoReceived.get())

        val tipThanksReceived = AtomicBoolean(false)
        atTestHelper.setOnAtAdded { at ->
            assertEquals("TipThanks", at.name)
            tipThanksReceived.set(true)
        }
        atTestHelper.addTipThanksAT(dp)
        assertTrue(tipThanksReceived.get())
        assertEquals(3, AT.getOrderedATs(dp).size.toLong())
    }
}
