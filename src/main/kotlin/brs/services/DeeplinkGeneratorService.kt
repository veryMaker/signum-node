package brs.services

interface DeeplinkGeneratorService {
    fun generateDeepLink(domain: String, action: String?, base64Payload: String?): String
}
