package brs.peer

import brs.Blockchain
import brs.util.convert.parseUnsignedLong
import brs.util.convert.toUnsignedString
import brs.util.logging.safeDebug
import brs.util.mustGetAsString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import kotlin.math.max
import kotlin.math.min

internal class GetMilestoneBlockIds(private val blockchain: Blockchain) : PeerServlet.PeerRequestHandler {
    override suspend fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        val response = JsonObject()
        try {
            val milestoneBlockIds = JsonArray()
            val lastBlockIdString = request.get("lastBlockId").mustGetAsString("lastBlockId")
            if (lastBlockIdString.isNotEmpty()) {
                val lastBlockId = lastBlockIdString.parseUnsignedLong()
                val myLastBlockId = blockchain.lastBlock.id
                if (myLastBlockId == lastBlockId || blockchain.hasBlock(lastBlockId)) {
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
            val blockchainHeight = blockchain.height
            val lastMilestoneBlockIdString = request.get("lastMilestoneBlockId").mustGetAsString("lastMilestoneBlockId")
            when {
                lastMilestoneBlockIdString.isEmpty() -> {
                    val lastMilestoneBlock = blockchain.getBlock(lastMilestoneBlockIdString.parseUnsignedLong())
                        ?: throw IllegalStateException("Don't have block $lastMilestoneBlockIdString")
                    height = lastMilestoneBlock.height
                    jump = min(1440, max(blockchainHeight - height, 1))
                    height = max(height - jump, 0)
                }
                lastBlockIdString.isNotEmpty() -> {
                    height = blockchainHeight
                    jump = 10
                }
                else -> {
                    peer.blacklist("GetMilestoneBlockIds")
                    response.addProperty("error", "Old getMilestoneBlockIds protocol not supported, please upgrade")
                    return response
                }
            }
            blockId = blockchain.getBlockIdAtHeight(height)

            while (height > 0 && limit-- > 0) {
                milestoneBlockIds.add(blockId.toUnsignedString())
                blockId = blockchain.getBlockIdAtHeight(height)
                height -= jump
            }
            response.add("milestoneBlockIds", milestoneBlockIds)
        } catch (e: RuntimeException) {
            logger.safeDebug { e.toString() }
            response.addProperty("error", e.toString())
        }

        return response
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GetMilestoneBlockIds::class.java)
    }
}
