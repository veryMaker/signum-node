package brs.deeplink

import com.google.zxing.WriterException
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.UnsupportedEncodingException

@RunWith(JUnit4::class)
class DeeplinkGeneratorTest {
    private lateinit var deeplinkGenerator: DeeplinkGenerator

    @Before
    fun setUpDeeplinkGeneratorTest() {
        deeplinkGenerator = DeeplinkGenerator()
    }

    @Test
    @Throws(UnsupportedEncodingException::class)
    fun testDeeplinkGenerator_Success() {
        val result = deeplinkGenerator.generateDeepLink("generic", "testAction", "dGVzdERhdGE=")
        val expectedResult = "burst.generic://v1?action=testAction&payload=dGVzdERhdGE%3D"
        assertEquals(expectedResult, result)
    }

    @Test
    @Throws(UnsupportedEncodingException::class)
    fun testDeeplinkGenerator_NoPayloadSuccess() {
        val result = deeplinkGenerator.generateDeepLink("generic", "testAction", null)
        val expectedResult = "burst.generic://v1?action=testAction"
        assertEquals(expectedResult, result)
    }

    @Test
    @Throws(UnsupportedEncodingException::class)
    fun testDeeplinkGenerator_InvalidDomain() {
        try {
            deeplinkGenerator.generateDeepLink("invalid", "testAction", null)
        } catch (e: IllegalArgumentException) {
            assertEquals(e.message, "Invalid domain:invalid")
        }

    }

    @Test
    @Throws(UnsupportedEncodingException::class)
    fun testDeeplinkGenerator_PayloadLengthExceeded() {

        val s = StringBuilder()
        for (i in 0..2048) {
            s.append("a")
        }

        try {
            deeplinkGenerator.generateDeepLink("generic", "testAction", s.toString())
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.startsWith("Maximum Payload Length "))
        }

    }

    @Test
    @Throws(WriterException::class, UnsupportedEncodingException::class)
    fun testDeeplinkGenerator_QrCode() {
        val bufferedImage = deeplinkGenerator.generateDeepLinkQrCode("generic", "testAction", "dGVzdERhdGE=")
        assertNotNull(bufferedImage)
    }
}
