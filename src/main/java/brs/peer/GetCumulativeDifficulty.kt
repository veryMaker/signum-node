package brs.peer

import brs.Blockchain
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class GetCumulativeDifficulty(private val blockchain: Blockchain) : PeerServlet.PeerRequestHandler {
    override suspend fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        val response = JsonObject()
        val lastBlock = blockchain.lastBlock
        response.addProperty("cumulativeDifficulty", lastBlock.cumulativeDifficulty.toString())
        response.addProperty("blockchainHeight", lastBlock.height)
        return response
    }
}
