package brs.deeplink

import brs.feesuggestions.FeeSuggestionType
import brs.util.StringUtils
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageConfig
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

import java.awt.image.BufferedImage
import java.util.EnumMap

class DeeplinkQRCodeGenerator { // TODO interface

    private val qrCodeWriter = QRCodeWriter()
    private val hints = EnumMap<EncodeHintType, ErrorCorrectionLevel>(EncodeHintType::class.java)

    init {
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
    }

    @Throws(WriterException::class)
    fun generateRequestBurstDeepLinkQRCode(receiverId: String, amountNQT: Long, feeSuggestionType: FeeSuggestionType?, feeNQT: Long?, message: String?, immutable: Boolean): BufferedImage {
        val deeplinkBuilder = StringBuilder("burst://requestBurst")

        deeplinkBuilder.append("&receiver=").append(receiverId)
        deeplinkBuilder.append("&amountNQT=").append(amountNQT)

        if (feeNQT != null) {
            deeplinkBuilder.append("&feeNQT=").append(feeNQT)
        } else if (feeSuggestionType != null) {
            deeplinkBuilder.append("&feeSuggestionType=").append(feeSuggestionType.type)
        }

        if (!message.isNullOrBlank()) {
            deeplinkBuilder.append("&message=").append(message)
        }

        deeplinkBuilder.append("&immutable=").append(immutable)

        return generateBurstQRCode(deeplinkBuilder.toString())
    }

    @Throws(WriterException::class)
    private fun generateBurstQRCode(url: String): BufferedImage {
        return MatrixToImageWriter.toBufferedImage(qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 350, 350, hints), MatrixToImageConfig())
    }
}
