package brs.peer

import brs.Constants.PROTOCOL
import brs.DependencyProvider
import brs.util.*
import brs.util.logging.safeDebug
import brs.util.logging.safeWarn
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import javax.servlet.ServletConfig
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class PeerServlet(private val dp: DependencyProvider) : HttpServlet() {
    private val peerRequestHandlers: Map<String, PeerRequestHandler>

    internal interface PeerRequestHandler {
        suspend fun processRequest(request: JsonObject, peer: Peer): JsonElement
    }

    internal abstract class ExtendedPeerRequestHandler : PeerRequestHandler {
        override suspend fun processRequest(request: JsonObject, peer: Peer): JsonElement {
            return JsonNull.INSTANCE
        }

        internal abstract suspend fun extendedProcessRequest(request: JsonObject, peer: Peer): ExtendedProcessRequest
    }

    internal class ExtendedProcessRequest(val response: JsonElement, val afterRequestHook: suspend () -> Unit)

    init { // TODO each one should take dp
        val map = mutableMapOf<String, PeerRequestHandler>()
        map["addPeers"] = AddPeers(dp)
        map["getCumulativeDifficulty"] = GetCumulativeDifficulty(dp.blockchain)
        map["getInfo"] = GetInfo(dp)
        map["getMilestoneBlockIds"] = GetMilestoneBlockIds(dp.blockchain)
        map["getNextBlockIds"] = GetNextBlockIds(dp.blockchain)
        map["getBlocksFromHeight"] = GetBlocksFromHeight(dp.blockchain)
        map["getNextBlocks"] = GetNextBlocks(dp.blockchain)
        map["getPeers"] = GetPeers(dp)
        map["getUnconfirmedTransactions"] = GetUnconfirmedTransactions(dp.transactionProcessor)
        map["processBlock"] = ProcessBlock(dp.blockchain, dp.blockchainProcessor)
        map["processTransactions"] = ProcessTransactions(dp.transactionProcessor)
        map["getAccountBalance"] = GetAccountBalance(dp.accountService)
        map["getAccountRecentTransactions"] = GetAccountRecentTransactions(dp.accountService, dp.blockchain)
        peerRequestHandlers = map
    }

    override fun init(config: ServletConfig) {
        super.init(config)
    }

    override fun doPost(request: HttpServletRequest, resp: HttpServletResponse) {
        try {
            if (!dp.peers.isSupportedUserAgent(request.getHeader("User-Agent"))) {
                return
            }
            runBlocking { // TODO
                process(request, resp)
            }
        } catch (e: Exception) { // We don't want to send exception information to client...
            resp.status = 500
            logger.safeWarn(e) { "Error handling peer request" }
        }

    }

    private suspend fun process(request: HttpServletRequest, resp: HttpServletResponse) {
        var peer: Peer? = null
        var response: JsonElement

        var extendedProcessRequest: ExtendedProcessRequest? = null

        var requestType = "unknown"
        try {
            peer = dp.peers.addPeer(request.remoteAddr, null)
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
                    dp.peers.updateAddress(peer)
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
                        response = runBlocking { // TODO
                            peerRequestHandler.processRequest(jsonRequest, peer)
                        }
                    }
                } else {
                    response = UNSUPPORTED_REQUEST_TYPE
                }
            } else {
                logger.safeDebug { "Unsupported protocol ${JSON.getAsString(jsonRequest.get(PROTOCOL))}" }
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
