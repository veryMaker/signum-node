package brs.services.impl

import brs.services.QRGeneratorService
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.awt.image.BufferedImage
import java.io.OutputStream

class QRGeneratorServiceImpl : QRGeneratorService {
    private val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L)
    private val writer = QRCodeWriter()

    private fun encodeMatrix(data: String): BitMatrix {
        return writer.encode(data, BarcodeFormat.QR_CODE, imageSize, imageSize, hints)
    }

    override fun generateQRCode(data: String): BufferedImage {
        return MatrixToImageWriter.toBufferedImage(encodeMatrix(data))
    }

    override fun writeQRCodeToStream(data: String, format: String, stream: OutputStream) {
        MatrixToImageWriter.writeToStream(encodeMatrix(data), format, stream)
    }

    companion object {
        private const val imageSize = 350 // Pixels on each side
    }
}
