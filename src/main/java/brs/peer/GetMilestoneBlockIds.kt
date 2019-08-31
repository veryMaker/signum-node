package brs.peer

import brs.Block
import brs.Blockchain
import brs.util.Convert
import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class GetMilestoneBlockIds(private val blockchain: Blockchain) : PeerServlet.PeerRequestHandler {

    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {

        val response = JsonObject()
        try {

            val milestoneBlockIds = JsonArray()

            val lastBlockIdString = JSON.getAsString(request.get("lastBlockId"))
            if (lastBlockIdString != null) {
                val lastBlockId = Convert.parseUnsignedLong(lastBlockIdString)
                val myLastBlockId = blockchain.lastBlock.id
                if (myLastBlockId == lastBlockId || blockchain.hasBlock(lastBlockId)) {
                    milestoneBlockIds.add(lastBlockIdString)
                    response.add("milestoneBlockIds", milestoneBlockIds)
                    if (myLastBlockId == lastBlockId) {
                        response.addProperty("last", java.lang.Boolean.TRUE)
                    }
                    return response
                }
            }

            var blockId: Long
            var height: Int
            val jump: Int
            var limit = 10
            val blockchainHeight = blockchain.height
            val lastMilestoneBlockIdString = JSON.getAsString(request.get("lastMilestoneBlockId"))
            if (lastMilestoneBlockIdString != null) {
                val lastMilestoneBlock = blockchain.getBlock(Convert.parseUnsignedLong(lastMilestoneBlockIdString))
                        ?: throw IllegalStateException("Don't have block $lastMilestoneBlockIdString")
                height = lastMilestoneBlock.height
                jump = Math.min(1440, Math.max(blockchainHeight - height, 1))
                height = Math.max(height - jump, 0)
            } else if (lastBlockIdString != null) {
                height = blockchainHeight
                jump = 10
            } else {
                peer.blacklist("GetMilestoneBlockIds")
                response.addProperty("error", "Old getMilestoneBlockIds protocol not supported, please upgrade")
                return response
            }
            blockId = blockchain.getBlockIdAtHeight(height)

            while (height > 0 && limit-- > 0) {
                milestoneBlockIds.add(Convert.toUnsignedLong(blockId))
                blockId = blockchain.getBlockIdAtHeight(height)
                height = height - jump
            }
            response.add("milestoneBlockIds", milestoneBlockIds)

        } catch (e: RuntimeException) {
            logger.debug(e.toString())
            response.addProperty("error", e.toString())
        }

        return response
    }

    companion object {

        private val logger = LoggerFactory.getLogger(GetMilestoneBlockIds::class.java)
    }

}
