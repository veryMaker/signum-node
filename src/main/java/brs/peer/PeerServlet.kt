package brs.peer

import brs.DependencyProvider
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory

import javax.servlet.ServletConfig
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.Collections

import brs.Constants.PROTOCOL
import brs.util.*
import com.google.gson.JsonNull

class PeerServlet(dp: DependencyProvider) : HttpServlet() {
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
        map["addPeers"] = AddPeers
        map["getCumulativeDifficulty"] = GetCumulativeDifficulty(dp.blockchain)
        map["getInfo"] = GetInfo(dp.timeService)
        map["getMilestoneBlockIds"] = GetMilestoneBlockIds(dp.blockchain)
        map["getNextBlockIds"] = GetNextBlockIds(dp.blockchain)
        map["getBlocksFromHeight"] = GetBlocksFromHeight(dp.blockchain)
        map["getNextBlocks"] = GetNextBlocks(dp.blockchain)
        map["getPeers"] = GetPeers
        map["getUnconfirmedTransactions"] = GetUnconfirmedTransactions(dp.transactionProcessor)
        map["processBlock"] = ProcessBlock(dp.blockchain, dp.blockchainProcessor)
        map["processTransactions"] = ProcessTransactions(dp.transactionProcessor)
        map["getAccountBalance"] = GetAccountBalance(dp.accountService)
        map["getAccountRecentTransactions"] = GetAccountRecentTransactions(dp.accountService, dp.blockchain)
        peerRequestHandlers = map
    }

    @Throws(ServletException::class)
    override fun init(config: ServletConfig) {
        super.init(config)
    }

    override fun doPost(request: HttpServletRequest, resp: HttpServletResponse) {
        try {
            if (!Peers.isSupportedUserAgent(request.getHeader("User-Agent"))) {
                return
            }
            process(request, resp)
        } catch (e: Exception) { // We don't want to send exception information to client...
            resp.status = 500
            logger.warn("Error handling peer request", e)
        }

    }

    @Throws(IOException::class)
    private fun process(request: HttpServletRequest, resp: HttpServletResponse) {
        var peer: Peer? = null
        var response: JsonElement

        var extendedProcessRequest: ExtendedProcessRequest? = null

        var requestType = "unknown"
        try {
            peer = Peers.addPeer(request.remoteAddr, null)
            if (peer == null || peer.isBlacklisted) {
                return
            }

            val cis = CountingInputStream(request.inputStream)
            val jsonRequest = InputStreamReader(cis, StandardCharsets.UTF_8).use { reader -> JSON.getAsJsonObject(reader.parseJson()) }
            if (jsonRequest.isEmpty()) {
                return
            }

            if (peer.state == Peer.State.DISCONNECTED) {
                peer.state = Peer.State.CONNECTED
                if (peer.announcedAddress != null) {
                    Peers.updateAddress(peer)
                }
            }
            peer.updateDownloadedVolume(cis.count)

            if (jsonRequest.get(PROTOCOL) != null && JSON.getAsString(jsonRequest.get(PROTOCOL)) == "B1") {
                requestType = "" + JSON.getAsString(jsonRequest.get("requestType"))
                val peerRequestHandler = peerRequestHandlers[JSON.getAsString(jsonRequest.get("requestType"))]
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
                if (logger.isDebugEnabled) {
                    logger.debug("Unsupported protocol {}", JSON.getAsString(jsonRequest.get(PROTOCOL)))
                }
                response = UNSUPPORTED_PROTOCOL
            }

        } catch (e: RuntimeException) {
            logger.debug("Error processing POST request", e)
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
