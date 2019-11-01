package brs.peer

import brs.entity.Block
import brs.services.BlockchainService
import brs.objects.Constants
import brs.util.convert.parseUnsignedLong
import brs.util.json.safeGetAsString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class GetNextBlocks(private val blockchainService: BlockchainService) : PeerServlet.PeerRequestHandler {

    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {

        val response = JsonObject()

        val nextBlocks = mutableListOf<Block>()
        var totalLength = 0
        val blockId = request.get("blockId").safeGetAsString().parseUnsignedLong()
        val blocks = blockchainService.getBlocksAfter(blockId, 100)

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
