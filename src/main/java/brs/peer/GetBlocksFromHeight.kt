package brs.peer

import brs.Block
import brs.Blockchain
import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class GetBlocksFromHeight(private val blockchain: Blockchain) : PeerServlet.PeerRequestHandler {
    override suspend fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        val response = JsonObject()
        var blockHeight = JSON.getAsInt(request.get("height"))
        var numBlocks = 100

        try {
            numBlocks = JSON.getAsInt(request.get("numBlocks"))
        } catch (ignored: Exception) {
        }

        // Small Failsafe
        if (numBlocks < 1 || numBlocks > 1400) {
            numBlocks = 100
        }
        if (blockHeight < 0) {
            blockHeight = 0
        }

        val blockId = blockchain.getBlockIdAtHeight(blockHeight)
        val blocks = blockchain.getBlocksAfter(blockId, numBlocks)
        val nextBlocksArray = JsonArray()
        for (nextBlock in blocks) {
            nextBlocksArray.add(nextBlock.jsonObject)
        }
        response.add("nextBlocks", nextBlocksArray)
        return response
    }
}
