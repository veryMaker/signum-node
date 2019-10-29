package brs.peer

import brs.services.TransactionProcessorService
import brs.api.http.common.ResultFields.UNCONFIRMED_TRANSACTIONS_RESPONSE
import brs.peer.PeerServlet.ExtendedProcessRequest
import com.google.gson.JsonArray
import com.google.gson.JsonObject

internal class GetUnconfirmedTransactions(private val transactionProcessorService: TransactionProcessorService) :
    PeerServlet.ExtendedPeerRequestHandler() {
    override fun extendedProcessRequest(request: JsonObject, peer: Peer): ExtendedProcessRequest {
        val response = JsonObject()

        val unconfirmedTransactions = transactionProcessorService.getAllUnconfirmedTransactionsFor(peer)

        val transactionsData = JsonArray()
        for (transaction in unconfirmedTransactions) {
            transactionsData.add(transaction.toJsonObject())
        }

        response.add(UNCONFIRMED_TRANSACTIONS_RESPONSE, transactionsData)

        return ExtendedProcessRequest(response) {
            transactionProcessorService.markFingerPrintsOf(
                peer,
                unconfirmedTransactions
            )
        }
    }
}
