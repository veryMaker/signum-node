package brs.services.impl

import brs.entity.FeeSuggestion
import brs.services.DeeplinkQRCodeGeneratorService
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageConfig
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.awt.image.BufferedImage
import java.util.*
class DeeplinkQRCodeGeneratorServiceImpl : DeeplinkQRCodeGeneratorService {
    private val qrCodeWriter = QRCodeWriter()
    private val hints = EnumMap<EncodeHintType, ErrorCorrectionLevel>(EncodeHintType::class.java)

    init {
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
    }

    override fun generateRequestBurstDeepLinkQRCode(
        receiverId: String,
        amountPlanck: Long,
        feeSuggestionType: FeeSuggestion.Type?,
        feePlanck: Long?,
        message: String?,
        immutable: Boolean
    ): BufferedImage {
        val deeplinkBuilder = StringBuilder("burst://requestBurst")

        deeplinkBuilder.append("&receiver=").append(receiverId)
        deeplinkBuilder.append("&amountPlanck=").append(amountPlanck)

        if (feePlanck != null) {
            deeplinkBuilder.append("&feePlanck=").append(feePlanck)
        } else if (feeSuggestionType != null) {
            deeplinkBuilder.append("&feeSuggestionType=").append(feeSuggestionType.type)
        }

        if (!message.isNullOrBlank()) {
            deeplinkBuilder.append("&message=").append(message)
        }

        deeplinkBuilder.append("&immutable=").append(immutable)

        return generateBurstQRCode(deeplinkBuilder.toString())
    }

    private fun generateBurstQRCode(url: String): BufferedImage {
        return MatrixToImageWriter.toBufferedImage(
            qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 350, 350, hints),
            MatrixToImageConfig()
        )
    }
}
