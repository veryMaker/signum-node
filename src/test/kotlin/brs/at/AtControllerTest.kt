package brs.at

import brs.entity.DependencyProvider
import brs.util.convert.parseHexString
import brs.util.convert.toHexString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class AtControllerTest {
    private lateinit var atTestHelper: AtTestHelper
    private lateinit var dp: DependencyProvider
    
    @Before
    fun setUp() {
        atTestHelper = AtTestHelper()
        dp = atTestHelper.setupMocks()
        dp.atController = AtController(dp)
    }

    @Test
    fun testCheckCreationBytes() {
        atTestHelper.clearAddedAts(dp)
        assertEquals(4, dp.atController.checkCreationBytes(AtTestHelper.HELLO_WORLD_CREATION_BYTES, Integer.MAX_VALUE).toLong())
        assertEquals(4, dp.atController.checkCreationBytes(AtTestHelper.ECHO_CREATION_BYTES, Integer.MAX_VALUE).toLong())
        assertEquals(5, dp.atController.checkCreationBytes(AtTestHelper.TIP_THANKS_CREATION_BYTES, Integer.MAX_VALUE).toLong())
    }

    @Test
    fun testRunSteps() {
        atTestHelper.clearAddedAts(dp)
        atTestHelper.addHelloWorldAT(dp)
        atTestHelper.addEchoAT(dp)
        atTestHelper.addTipThanksAT(dp)
        assertEquals(3, dp.atStore.getOrderedATs().size.toLong())
        val atBlock = dp.atController.getCurrentBlockATs(Integer.MAX_VALUE, Integer.MAX_VALUE)
        assertNotNull(atBlock)
        assertNotNull(atBlock.bytesForBlock)
        assertEquals("010000000000000097c1d1e5b25c1d109f2ba522d1dda248020000000000000014ea12712c274caebc49ccd7fff0b0b703000000000000009f1af5443c8d1e7b492f848e91fccb1f", atBlock.bytesForBlock.toHexString())
    }

    @Test
    fun testValidateAts() {
        atTestHelper.clearAddedAts(dp)
        atTestHelper.addHelloWorldAT(dp)
        atTestHelper.addEchoAT(dp)
        atTestHelper.addTipThanksAT(dp)
        assertEquals(3, dp.atStore.getOrderedATs().size.toLong())
        val atBlock = dp.atController.validateATs("010000000000000097c1d1e5b25c1d109f2ba522d1dda248020000000000000014ea12712c274caebc49ccd7fff0b0b703000000000000009f1af5443c8d1e7b492f848e91fccb1f".parseHexString(), Integer.MAX_VALUE)
        assertNotNull(atBlock)
        assertEquals(0, atBlock.totalAmountPlanck)
        assertEquals(5439000, atBlock.totalFees)
    }
}
