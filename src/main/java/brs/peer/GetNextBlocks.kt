package brs.peer

import brs.Block
import brs.Blockchain
import brs.Constants
import brs.util.Convert
import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import java.util.ArrayList

internal class GetNextBlocks(private val blockchain: Blockchain) : PeerServlet.PeerRequestHandler {


    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {

        val response = JsonObject()

        val nextBlocks = ArrayList<Block>()
        var totalLength = 0
        val blockId = Convert.parseUnsignedLong(JSON.getAsString(request.get("blockId")))
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
            nextBlocksArray.add(nextBlock.jsonObject)
        }
        response.add("nextBlocks", nextBlocksArray)

        return response
    }

}
