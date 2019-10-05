package brs.deeplink

import brs.common.TestConstants
import brs.feesuggestions.FeeSuggestionType
import com.google.zxing.WriterException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import java.awt.image.BufferedImage

import org.junit.Assert.assertNotNull

@RunWith(JUnit4::class)
class DeeplinkQRCodeGeneratorTest {
    private lateinit var deeplinkQRCodeGenerator: DeeplinkQRCodeGenerator

    @Before
    fun setUpDeeplinkQrCodeGeneratorTest() {
        deeplinkQRCodeGenerator = DeeplinkQRCodeGenerator()
    }

    @Test
    fun testDeeplinkQrCodeGenerator() {
        val image = deeplinkQRCodeGenerator!!.generateRequestBurstDeepLinkQRCode(TestConstants.TEST_ACCOUNT_NUMERIC_ID, TestConstants.TEN_BURST, FeeSuggestionType.STANDARD, 0L, "Test!", true)
        assertNotNull(image)
    }
}
