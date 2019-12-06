package brs.peer

import brs.api.http.common.ResultFields.UNCONFIRMED_TRANSACTIONS_RESPONSE
import brs.entity.DependencyProvider
import brs.peer.PeerServlet.ExtendedProcessRequest
import com.google.gson.JsonArray
import com.google.gson.JsonObject

internal class GetUnconfirmedTransactions(private val dp: DependencyProvider) :
    PeerServlet.ExtendedPeerRequestHandler() {
    override fun extendedProcessRequest(request: JsonObject, peer: Peer): ExtendedProcessRequest {
        val response = JsonObject()

        val unconfirmedTransactions = dp.unconfirmedTransactionService.getAllFor(peer)

        val transactionsData = JsonArray()
        for (transaction in unconfirmedTransactions) {
            transactionsData.add(transaction.toJsonObject())
        }

        response.add(UNCONFIRMED_TRANSACTIONS_RESPONSE, transactionsData)

        return ExtendedProcessRequest(response) {
            dp.unconfirmedTransactionService.markFingerPrintsOf(
                peer,
                unconfirmedTransactions
            )
        }
    }
}
