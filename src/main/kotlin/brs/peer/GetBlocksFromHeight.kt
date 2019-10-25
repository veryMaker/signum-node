package brs.peer

import brs.services.BlockchainService
import brs.util.mustGetAsInt
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class GetBlocksFromHeight(private val blockchainService: BlockchainService) : PeerServlet.PeerRequestHandler {
    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        val response = JsonObject()
        var blockHeight = request.get("height").mustGetAsInt("numBlocks")
        var numBlocks = 100

        try {
            numBlocks = request.get("numBlocks").mustGetAsInt("numBlocks")
        } catch (ignored: Exception) {
        }

        // Small Failsafe
        if (numBlocks < 1 || numBlocks > 1400) {
            numBlocks = 100
        }
        if (blockHeight < 0) {
            blockHeight = 0
        }

        val blockId = blockchainService.getBlockIdAtHeight(blockHeight)
        val blocks = blockchainService.getBlocksAfter(blockId, numBlocks)
        val nextBlocksArray = JsonArray()
        for (nextBlock in blocks) {
            nextBlocksArray.add(nextBlock.toJsonObject())
        }
        response.add("nextBlocks", nextBlocksArray)
        return response
    }
}
