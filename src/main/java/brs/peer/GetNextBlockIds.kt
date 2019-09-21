package brs.peer

import brs.Blockchain
import brs.util.JSON
import brs.util.parseUnsignedLong
import brs.util.toUnsignedString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class GetNextBlockIds(private val blockchain: Blockchain) : PeerServlet.PeerRequestHandler {


    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {

        val response = JsonObject()

        val nextBlockIds = JsonArray()
        val blockId = JSON.getAsString(request.get("blockId")).parseUnsignedLong()
        val ids = blockchain.getBlockIdsAfter(blockId, 100)

        for (id in ids) {
            nextBlockIds.add(id.toUnsignedString())
        }

        response.add("nextBlockIds", nextBlockIds)

        return response
    }

}
