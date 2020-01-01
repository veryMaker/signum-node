package brs.peer

import brs.services.BlockchainService
import brs.util.convert.parseUnsignedLong
import brs.util.convert.toUnsignedString
import brs.util.json.getMemberAsString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class GetNextBlockIds(private val blockchainService: BlockchainService) : PeerServlet.PeerRequestHandler {
    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        val response = JsonObject()

        val nextBlockIds = JsonArray()
        val blockId = request.getMemberAsString("blockId").parseUnsignedLong()
        val ids = blockchainService.getBlockIdsAfter(blockId, 100)

        for (id in ids) {
            nextBlockIds.add(id.toUnsignedString())
        }

        response.add("nextBlockIds", nextBlockIds)

        return response
    }
}
