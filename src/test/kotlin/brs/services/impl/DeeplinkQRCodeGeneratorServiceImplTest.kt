package brs.services.impl

import brs.common.TestConstants
import brs.entity.FeeSuggestion
import brs.services.impl.DeeplinkQRCodeGeneratorServiceImpl
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DeeplinkQRCodeGeneratorServiceImplTest {
    private lateinit var deeplinkQRCodeGeneratorServiceImpl: DeeplinkQRCodeGeneratorServiceImpl

    @Before
    fun setUpDeeplinkQrCodeGeneratorTest() {
        deeplinkQRCodeGeneratorServiceImpl = DeeplinkQRCodeGeneratorServiceImpl()
    }

    @Test
    fun testDeeplinkQrCodeGenerator() {
        val image = deeplinkQRCodeGeneratorServiceImpl.generateRequestBurstDeepLinkQRCode(TestConstants.TEST_ACCOUNT_NUMERIC_ID, TestConstants.TEN_BURST, FeeSuggestion.Type.STANDARD, 0L, "Test!", true)
        assertNotNull(image)
    }
}
