package brs.peer

import brs.entity.DependencyProvider
import brs.objects.Constants.PROTOCOL
import brs.util.CountingInputStream
import brs.util.CountingOutputStream
import brs.util.json.*
import brs.util.logging.safeDebug
import brs.util.logging.safeWarn
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.collections.set

class PeerServlet(private val dp: DependencyProvider) : HttpServlet() {
    private val peerRequestHandlers: Map<String, PeerRequestHandler>

    interface PeerRequestHandler {
        fun processRequest(request: JsonObject, peer: Peer): JsonElement
    }

    init { // TODO each one should take dp
        val map = mutableMapOf<String, PeerRequestHandler>()
        map["addPeers"] = AddPeers(dp)
        map["getCumulativeDifficulty"] = GetCumulativeDifficulty(dp.blockchainService)
        map["getInfo"] = GetInfo(dp)
        map["getMilestoneBlockIds"] = GetMilestoneBlockIds(dp.blockchainService)
        map["getNextBlocks"] = GetNextBlocks(dp.blockchainService)
        map["getNextBlockIds"] = GetNextBlockIds(dp.blockchainService)
        map["getPeers"] = GetPeers(dp)
        map["getUnconfirmedTransactions"] = GetUnconfirmedTransactions(dp)
        map["processBlock"] = ProcessBlock(dp)
        map["processTransactions"] = ProcessTransactions(dp.transactionProcessorService)
        peerRequestHandlers = map
    }

    override fun doPost(request: HttpServletRequest, resp: HttpServletResponse) {
        try {
            if (!dp.peerService.isSupportedUserAgent(request.getHeader("User-Agent"))) {
                return
            }
            process(request, resp)
        } catch (e: Exception) { // We don't want to send exception information to client...
            resp.status = 500
            logger.safeWarn(e) { "Error handling peer request" }
        }
    }

    private fun process(request: HttpServletRequest, resp: HttpServletResponse) {
        var peer: Peer? = null
        var response: JsonElement

        var requestType = "unknown"
        try {
            peer = dp.peerService.getOrAddPeer(request.remoteAddr, null)
            if (peer == null || peer.isBlacklisted) {
                return
            }

            val cis = CountingInputStream(request.inputStream)
            val jsonRequest = InputStreamReader(cis, StandardCharsets.UTF_8).use { reader ->
                reader.parseJson().safeGetAsJsonObject()
            }
            if (jsonRequest == null || jsonRequest.isEmpty()) {
                return
            }

            if (peer.state == Peer.State.DISCONNECTED) {
                peer.state = Peer.State.CONNECTED
                if (peer.announcedAddress != null) {
                    dp.peerService.updateAddress(peer)
                }
            }
            peer.updateDownloadedVolume(cis.count)

            if (jsonRequest.getMemberAsString(PROTOCOL) == "B1") {
                requestType = jsonRequest.mustGetMemberAsString("requestType")
                val peerRequestHandler = peerRequestHandlers[requestType]
                response = peerRequestHandler?.processRequest(jsonRequest, peer) ?: UNSUPPORTED_REQUEST_TYPE
            } else {
                logger.safeDebug { "Unsupported protocol ${jsonRequest.getMemberAsString(PROTOCOL)}" }
                response = UNSUPPORTED_PROTOCOL
            }
        } catch (e: Exception) {
            logger.safeDebug(e) { "Error processing POST request" }
            val json = JsonObject()
            json.addProperty("error", e.toString())
            response = json
        }

        resp.contentType = "text/plain; charset=UTF-8"
        try {
            val byteCount: Long
            val cos = CountingOutputStream(resp.outputStream)
            OutputStreamWriter(cos, StandardCharsets.UTF_8).use { writer -> response.writeTo(writer) }
            byteCount = cos.count
            peer?.updateUploadedVolume(byteCount)
        } catch (e: Exception) {
            peer?.blacklist(e, "can't respond to requestType=$requestType")
            return
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PeerServlet::class.java)

        private val UNSUPPORTED_REQUEST_TYPE: JsonElement

        init {
            val response = JsonObject()
            response.addProperty("error", "Unsupported request type!")
            UNSUPPORTED_REQUEST_TYPE = response
        }

        private val UNSUPPORTED_PROTOCOL: JsonElement

        init {
            val response = JsonObject()
            response.addProperty("error", "Unsupported protocol!")
            UNSUPPORTED_PROTOCOL = response
        }
    }
}
