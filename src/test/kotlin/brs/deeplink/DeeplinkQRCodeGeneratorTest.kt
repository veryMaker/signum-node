package brs.deeplink

import brs.common.TestConstants
import brs.feesuggestions.FeeSuggestionType
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DeeplinkQRCodeGeneratorTest {
    private lateinit var deeplinkQRCodeGenerator: DeeplinkQRCodeGenerator

    @Before
    fun setUpDeeplinkQrCodeGeneratorTest() {
        deeplinkQRCodeGenerator = DeeplinkQRCodeGenerator()
    }

    @Test
    fun testDeeplinkQrCodeGenerator() {
        val image = deeplinkQRCodeGenerator.generateRequestBurstDeepLinkQRCode(TestConstants.TEST_ACCOUNT_NUMERIC_ID, TestConstants.TEN_BURST, FeeSuggestionType.STANDARD, 0L, "Test!", true)
        assertNotNull(image)
    }
}
