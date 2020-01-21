package brs.peer

import brs.entity.Block
import brs.entity.DependencyProvider
import brs.util.BurstException
import brs.util.json.getMemberAsString
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class ProcessBlock(private val dp: DependencyProvider) : PeerServlet.PeerRequestHandler {
    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        try {
            if (dp.blockchainService.lastBlock.stringId != request.getMemberAsString("previousBlock")) {
                // do this check first to avoid validation failures of future blocks and transactions
                // when loading blockchain from scratch
                return NOT_ACCEPTED
            }
            dp.blockchainProcessorService.processPeerBlock(Block.parseBlock(dp, request, dp.blockchainService.height), peer)
            return ACCEPTED
        } catch (e: BurstException) {
            peer.blacklist(e, "received invalid data via requestType=processBlock")
            return NOT_ACCEPTED
        } catch (e: Exception) {
            peer.blacklist(e, "received invalid data via requestType=processBlock")
            return NOT_ACCEPTED
        }
    }

    companion object {
        private val ACCEPTED: JsonElement

        init {
            val response = JsonObject()
            response.addProperty("accepted", true)
            ACCEPTED = response
        }

        private val NOT_ACCEPTED: JsonElement

        init {
            val response = JsonObject()
            response.addProperty("accepted", false)
            NOT_ACCEPTED = response
        }
    }
}
