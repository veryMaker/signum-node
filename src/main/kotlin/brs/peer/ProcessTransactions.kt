package brs.peer

import brs.entity.DependencyProvider
import brs.entity.Transaction
import brs.util.json.getMemberAsJsonArray
import brs.util.json.mustGetAsJsonObject
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class ProcessTransactions(private val dp: DependencyProvider) : PeerServlet.PeerRequestHandler {
    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        return try {
            val transactions = (request.getMemberAsJsonArray("transactions") ?: return JsonObject())
                .map { Transaction.parseTransaction(dp, it.mustGetAsJsonObject("transaction")) }
            dp.transactionProcessorService.processPeerTransactions(transactions, peer) // TODO this is not locking sync obj...
            JsonObject()
        } catch (e: Exception) {
            peer.blacklist(e, "received invalid data via requestType=processTransactions")
            val response = JsonObject()
            response.addProperty("error", e.toString())
            response
        }
    }
}
