package brs.peer

import brs.Blockchain
import brs.util.convert.parseUnsignedLong
import brs.util.convert.toUnsignedString
import brs.util.safeGetAsString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class GetNextBlockIds(private val blockchain: Blockchain) : PeerServlet.PeerRequestHandler {


    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {

        val response = JsonObject()

        val nextBlockIds = JsonArray()
        val blockId = request.get("blockId").safeGetAsString().parseUnsignedLong()
        val ids = blockchain.getBlockIdsAfter(blockId, 100)

        for (id in ids) {
            nextBlockIds.add(id.toUnsignedString())
        }

        response.add("nextBlockIds", nextBlockIds)

        return response
    }

}
