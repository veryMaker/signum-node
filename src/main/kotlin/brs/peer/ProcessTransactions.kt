package brs.peer

import brs.util.BurstException
import brs.services.TransactionProcessorService
import brs.util.JSON
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class ProcessTransactions(private val transactionProcessorService: TransactionProcessorService) : PeerServlet.PeerRequestHandler {
    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        return try {
            transactionProcessorService.processPeerTransactions(request, peer) // TODO this is not locking sync obj...
            JSON.emptyJSON
        } catch (e: RuntimeException) {
            peer.blacklist(e, "received invalid data via requestType=processTransactions")
            val response = JsonObject()
            response.addProperty("error", e.toString())
            response
        } catch (e: BurstException.ValidationException) {
            peer.blacklist(e, "received invalid data via requestType=processTransactions")
            val response = JsonObject()
            response.addProperty("error", e.toString())
            response
        }
    }
}
