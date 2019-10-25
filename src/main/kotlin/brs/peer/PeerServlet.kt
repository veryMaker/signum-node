package brs.peer

import brs.objects.Constants.PROTOCOL
import brs.DependencyProvider
import brs.util.*
import brs.util.logging.safeDebug
import brs.util.logging.safeWarn
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class PeerServlet(private val dp: DependencyProvider) : HttpServlet() {
    private val peerRequestHandlers: Map<String, PeerRequestHandler>

    internal interface PeerRequestHandler {
        fun processRequest(request: JsonObject, peer: Peer): JsonElement
    }

    internal abstract class ExtendedPeerRequestHandler : PeerRequestHandler {
        override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
            return JsonNull.INSTANCE
        }

        internal abstract fun extendedProcessRequest(request: JsonObject, peer: Peer): ExtendedProcessRequest
    }

    internal class ExtendedProcessRequest(val response: JsonElement, val afterRequestHook: () -> Unit)

    init { // TODO each one should take dp
        val map = mutableMapOf<String, PeerRequestHandler>()
        map["addPeers"] = AddPeers(dp)
        map["getCumulativeDifficulty"] = GetCumulativeDifficulty(dp.blockchainService)
        map["getInfo"] = GetInfo(dp)
        map["getMilestoneBlockIds"] = GetMilestoneBlockIds(dp.blockchainService)
        map["getNextBlockIds"] = GetNextBlockIds(dp.blockchainService)
        map["getBlocksFromHeight"] = GetBlocksFromHeight(dp.blockchainService)
        map["getNextBlocks"] = GetNextBlocks(dp.blockchainService)
        map["getPeers"] = GetPeers(dp)
        map["getUnconfirmedTransactions"] = GetUnconfirmedTransactions(dp.transactionProcessorService)
        map["processBlock"] = ProcessBlock(dp.blockchainService, dp.blockchainProcessorService)
        map["processTransactions"] = ProcessTransactions(dp.transactionProcessorService)
        map["getAccountBalance"] = GetAccountBalance(dp.accountService)
        map["getAccountRecentTransactions"] = GetAccountRecentTransactions(dp.accountService, dp.blockchainService)
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

        var extendedProcessRequest: ExtendedProcessRequest? = null

        var requestType = "unknown"
        try {
            peer = dp.peerService.addPeer(request.remoteAddr, null)
            if (peer == null || peer.isBlacklisted) {
                return
            }

            val cis = CountingInputStream(request.inputStream)
            val jsonRequest = InputStreamReader(cis, StandardCharsets.UTF_8).use { reader -> reader.parseJson().mustGetAsJsonObject("request") }
            if (jsonRequest.isEmpty()) {
                return
            }

            if (peer.state == Peer.State.DISCONNECTED) {
                peer.state = Peer.State.CONNECTED
                if (peer.announcedAddress != null) {
                    dp.peerService.updateAddress(peer)
                }
            }
            peer.updateDownloadedVolume(cis.count)

            if (jsonRequest.get(PROTOCOL) != null && jsonRequest.get(PROTOCOL).safeGetAsString() == "B1") {
                requestType = jsonRequest.get("requestType").mustGetAsString("requestType")
                val peerRequestHandler = peerRequestHandlers[requestType]
                if (peerRequestHandler != null) {
                    if (peerRequestHandler is ExtendedPeerRequestHandler) {
                        extendedProcessRequest = peerRequestHandler.extendedProcessRequest(jsonRequest, peer)
                        response = extendedProcessRequest.response
                    } else {
                        response = peerRequestHandler.processRequest(jsonRequest, peer)
                    }
                } else {
                    response = UNSUPPORTED_REQUEST_TYPE
                }
            } else {
                logger.safeDebug { "Unsupported protocol ${jsonRequest.get(PROTOCOL).safeGetAsString()}" }
                response = UNSUPPORTED_PROTOCOL
            }

        } catch (e: RuntimeException) {
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

        extendedProcessRequest?.afterRequestHook?.invoke()
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
