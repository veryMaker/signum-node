package brs.peer

import brs.Block
import brs.Blockchain
import brs.Constants
import brs.util.convert.parseUnsignedLong
import brs.util.safeGetAsString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class GetNextBlocks(private val blockchain: Blockchain) : PeerServlet.PeerRequestHandler {

    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {

        val response = JsonObject()

        val nextBlocks = mutableListOf<Block>()
        var totalLength = 0
        val blockId = request.get("blockId").safeGetAsString().parseUnsignedLong()
        val blocks = blockchain.getBlocksAfter(blockId, 100)

        for (block in blocks) {
            val length = Constants.BLOCK_HEADER_LENGTH + block.payloadLength
            if (totalLength + length > 1048576) {
                break
            }
            nextBlocks.add(block)
            totalLength += length
        }

        val nextBlocksArray = JsonArray()
        for (nextBlock in nextBlocks) {
            nextBlocksArray.add(nextBlock.toJsonObject())
        }
        response.add("nextBlocks", nextBlocksArray)

        return response
    }

}
