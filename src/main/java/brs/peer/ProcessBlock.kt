package brs.peer

import brs.Blockchain
import brs.BlockchainProcessor
import brs.BurstException
import brs.util.JSON
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class ProcessBlock(private val blockchain: Blockchain, private val blockchainProcessor: BlockchainProcessor) : PeerServlet.PeerRequestHandler {

    override suspend fun processRequest(request: JsonObject, peer: Peer): JsonElement {

        try {
            if (blockchain.lastBlock.stringId != JSON.getAsString(request.get("previousBlock"))) {
                // do this check first to avoid validation failures of future blocks and transactions
                // when loading blockchain from scratch
                return NOT_ACCEPTED
            }
            blockchainProcessor.processPeerBlock(request, peer)
            return ACCEPTED

        } catch (e: BurstException) {
            peer.blacklist(e, "received invalid data via requestType=processBlock")
            return NOT_ACCEPTED
        } catch (e: RuntimeException) {
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
