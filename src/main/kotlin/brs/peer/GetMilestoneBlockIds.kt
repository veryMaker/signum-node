package brs.peer

import brs.services.BlockchainService
import brs.util.convert.parseUnsignedLong
import brs.util.convert.toUnsignedString
import brs.util.json.safeGetAsString
import brs.util.logging.safeDebug
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import kotlin.math.max
import kotlin.math.min

internal class GetMilestoneBlockIds(private val blockchainService: BlockchainService) : PeerServlet.PeerRequestHandler {
    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        val response = JsonObject()
        try {
            val milestoneBlockIds = JsonArray()
            val lastBlockIdString = request.get("lastBlockId").safeGetAsString()
            if (!lastBlockIdString.isNullOrEmpty()) {
                val lastBlockId = lastBlockIdString.parseUnsignedLong()
                val myLastBlockId = blockchainService.lastBlock.id
                if (myLastBlockId == lastBlockId || blockchainService.hasBlock(lastBlockId)) {
                    milestoneBlockIds.add(lastBlockIdString)
                    response.add("milestoneBlockIds", milestoneBlockIds)
                    if (myLastBlockId == lastBlockId) {
                        response.addProperty("last", true)
                    }
                    return response
                }
            }

            var blockId: Long
            var height: Int
            val jump: Int
            var limit = 10
            val blockchainHeight = blockchainService.height
            val lastMilestoneBlockIdString = request.get("lastMilestoneBlockId").safeGetAsString()
            if (!lastMilestoneBlockIdString.isNullOrEmpty()) {
                val lastMilestoneBlock = blockchainService.getBlock(lastMilestoneBlockIdString.parseUnsignedLong()) ?: error("Don't have block $lastMilestoneBlockIdString")
                height = lastMilestoneBlock.height
                jump = min(1440, max(blockchainHeight - height, 1))
                height = max(height - jump, 0)
            } else if (lastBlockIdString != null) {
                height = blockchainHeight
                jump = 10
            } else {
                peer.blacklist("GetMilestoneBlockIds");
                response.addProperty("error", "Old getMilestoneBlockIds protocol not supported, please upgrade");
                return response
            }
            blockId = blockchainService.getBlockIdAtHeight(height)

            while (height > 0 && limit-- > 0) {
                milestoneBlockIds.add(blockId.toUnsignedString())
                blockId = blockchainService.getBlockIdAtHeight(height)
                height -= jump
            }
            response.add("milestoneBlockIds", milestoneBlockIds)
        } catch (e: Exception) {
            logger.safeDebug { e.toString() }
            response.addProperty("error", e.toString())
        }

        return response
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GetMilestoneBlockIds::class.java)
    }
}
