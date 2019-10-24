package brs.peer

import brs.Blockchain
import brs.http.JSONData
import brs.services.AccountService
import brs.util.JSON
import brs.util.convert.parseAccountId
import brs.util.mustGetAsString
import brs.util.safeGetAsString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject


@Deprecated("This call is no longer made by the other peers so will soon be removed.")
class GetAccountRecentTransactions internal constructor(private val accountService: AccountService, private val blockchain: Blockchain) : PeerServlet.PeerRequestHandler {
    override fun processRequest(request: JsonObject, peer: Peer): JsonElement {
        val response = JsonObject()
        val accountId = request.get("account").mustGetAsString("account").parseAccountId()
        val account = accountService.getAccount(accountId)
        val transactions = JsonArray()
        if (account != null) {
            for (transaction in blockchain.getTransactions(account, 0, (-1).toByte(), 0.toByte(), 0, 0, 9, false)) {
                transactions.add(JSONData.transaction(transaction, blockchain.height))
            }
        }
        response.add("transactions", transactions)

        return response
    }
}
