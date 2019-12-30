package brs.services.impl

import brs.services.DeeplinkGeneratorService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class DeeplinkGeneratorServiceImpl : DeeplinkGeneratorService {
    private val validDomains = listOf("payment", "message", "contract", "asset", "market", "generic")

    override fun generateDeepLink(domain: String, action: String?, base64Payload: String?): String {
        require(validDomains.contains(domain)) { "Invalid domain: \"$domain\"" }

        val deeplinkBuilder = StringBuilder("burst.")
            .append(domain)
            .append("://$Version") // This will be inlined by the compiler so only one append needed
        if (action != null) {
            deeplinkBuilder.append("?action=")
                .append(action)
            if (base64Payload != null) {
                deeplinkBuilder.append("&payload=")
                val encodedPayload = URLEncoder.encode(base64Payload, StandardCharsets.UTF_8.toString())
                require(encodedPayload.length <= MaxPayloadLength) { "Maximum Payload Length ($MaxPayloadLength) exceeded" }
                deeplinkBuilder.append(encodedPayload)
            }
        }
        return deeplinkBuilder.toString()
    }

    companion object {
        private const val Version = "v1"
        private const val MaxPayloadLength = 2048
    }
}
