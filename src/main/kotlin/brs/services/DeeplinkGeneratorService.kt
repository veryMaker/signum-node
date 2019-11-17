package brs.services

import java.awt.image.BufferedImage

interface DeeplinkGeneratorService {
    fun generateDeepLink(domain: String, action: String?, base64Payload: String?): String
    fun generateDeepLinkQrCode(domain: String, action: String?, base64Payload: String?): BufferedImage
}