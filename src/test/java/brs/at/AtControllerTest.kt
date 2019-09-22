package brs.at

import brs.Account
import brs.Burst
import brs.util.Convert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class AtControllerTest {
    @Before
    fun setUp() {
        AtTestHelper.setupMocks()
    }

    @Test
    fun testCheckCreationBytes() {
        AtTestHelper.clearAddedAts()
        assertEquals(4, dp.atController.checkCreationBytes(AtTestHelper.HELLO_WORLD_CREATION_BYTES, Integer.MAX_VALUE).toLong())
        assertEquals(4, dp.atController.checkCreationBytes(AtTestHelper.ECHO_CREATION_BYTES, Integer.MAX_VALUE).toLong())
        assertEquals(5, dp.atController.checkCreationBytes(AtTestHelper.TIP_THANKS_CREATION_BYTES, Integer.MAX_VALUE).toLong())
    }

    @Test
    fun testRunSteps() {
        AtTestHelper.clearAddedAts()
        AtTestHelper.addHelloWorldAT()
        AtTestHelper.addEchoAT()
        AtTestHelper.addTipThanksAT()
        assertEquals(3, AT.getOrderedATs().size.toLong())
        val atBlock = dp.atController.getCurrentBlockATs(Integer.MAX_VALUE, Integer.MAX_VALUE)
        assertNotNull(atBlock)
        assertNotNull(atBlock.bytesForBlock)
        assertEquals("010000000000000097c1d1e5b25c1d109f2ba522d1dda248020000000000000014ea12712c274caebc49ccd7fff0b0b703000000000000009f1af5443c8d1e7b492f848e91fccb1f", atBlock.bytesForBlock.toHexString())
    }

    @Test
    fun testValidateAts() {
        AtTestHelper.clearAddedAts()
        AtTestHelper.addHelloWorldAT()
        AtTestHelper.addEchoAT()
        AtTestHelper.addTipThanksAT()
        assertEquals(3, AT.getOrderedATs().size.toLong())
        val atBlock = dp.atController.validateATs(Convert."010000000000000097c1d1e5b25c1d109f2ba522d1dda248020000000000000014ea12712c274caebc49ccd7fff0b0b703000000000000009f1af5443c8d1e7b492f848e91fccb1f"), Integer.MAX_VALUE.parseHexString()
        assertNotNull(atBlock)
        assertEquals(0, atBlock.totalAmount)
        assertEquals(5439000, atBlock.totalFees)
    }
}
