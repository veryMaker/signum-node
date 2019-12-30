package brs.services

import java.awt.image.BufferedImage
import java.io.OutputStream

interface QRGeneratorService {
    fun generateQRCode(data: String): BufferedImage
    fun writeQRCodeToStream(data: String, format: String, stream: OutputStream)
}
