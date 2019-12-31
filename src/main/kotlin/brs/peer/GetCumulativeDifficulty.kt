package brs.peer

import brs.services.BlockchainService
import brs.util.jetty.get
import com.google.gson.JsonElement
import brs.util.jetty.get
import com.google.gson.JsonObject

internal class GetCumulativeDifficulty(private val blockchainService: BlockchainService) :
    PeerServlet.PeerRequestHandler {
    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        val response = JsonObject()
        val lastBlock = blockchainService.lastBlock
        response.addProperty("cumulativeDifficulty", lastBlock.cumulativeDifficulty.toString())
        response.addProperty("blockchainHeight", lastBlock.height)
        return response
    }
}
