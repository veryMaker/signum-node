package brs.at

import brs.Account
import brs.Burst
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.util.concurrent.atomic.AtomicBoolean

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.util.function.Consumer

class ATTest {
    @Before
    fun setUp() {
        AtTestHelper.setupMocks()
    }

    @Test
    fun testAddAt() {
        AtTestHelper.clearAddedAts()
        val helloWorldReceived = AtomicBoolean(false)
        AtTestHelper.setOnAtAdded(Consumer { at ->
            assertEquals("HelloWorld", at.name)
            helloWorldReceived.set(true)
        })
        AtTestHelper.addHelloWorldAT()
        assertTrue(helloWorldReceived.get())

        val echoReceived = AtomicBoolean(false)
        AtTestHelper.setOnAtAdded(Consumer { at ->
            assertEquals("Echo", at.name)
            echoReceived.set(true)
        })
        AtTestHelper.addEchoAT()
        assertTrue(echoReceived.get())

        val tipThanksReceived = AtomicBoolean(false)
        AtTestHelper.setOnAtAdded(Consumer { at ->
            assertEquals("TipThanks", at.name)
            tipThanksReceived.set(true)
        })
        AtTestHelper.addTipThanksAT()
        assertTrue(tipThanksReceived.get())
        assertEquals(3, AT.getOrderedATs().size.toLong())
    }
}
