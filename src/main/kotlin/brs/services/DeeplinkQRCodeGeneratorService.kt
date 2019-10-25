package brs.services

import brs.entity.FeeSuggestion
import java.awt.image.BufferedImage

interface DeeplinkQRCodeGeneratorService {
    fun generateRequestBurstDeepLinkQRCode(receiverId: String, amountPlanck: Long, feeSuggestionType: FeeSuggestion.Type?, feePlanck: Long?, message: String?, immutable: Boolean): BufferedImage
}