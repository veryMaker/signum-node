package brs.services.impl

import brs.services.QRGeneratorService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.ByteArrayOutputStream

@RunWith(JUnit4::class)
class QRGeneratorServiceTest {
    private lateinit var qrGeneratorService: QRGeneratorService

    @Before
    fun setUp() {
        qrGeneratorService = QRGeneratorServiceImpl()
    }

    @Test
    fun testGenerateQRCode() {
        val bufferedImage = qrGeneratorService.generateQRCode("hello")
        bufferedImage.hashCode()
    }

    @Test
    fun testWriteQRCodeToStream() {
        val outputStream = ByteArrayOutputStream()
        qrGeneratorService.writeQRCodeToStream("hello", "png", outputStream)
    }
}