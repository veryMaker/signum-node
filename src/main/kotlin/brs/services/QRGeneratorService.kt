package brs.services

import java.awt.image.BufferedImage
import java.io.OutputStream

interface QRGeneratorService {
    /**
     * TODO
     */
    fun generateQRCode(data: String): BufferedImage

    /**
     * TODO
     */
    fun writeQRCodeToStream(data: String, format: String, stream: OutputStream)
}
