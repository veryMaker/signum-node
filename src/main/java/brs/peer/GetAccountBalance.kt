package brs.peer

import brs.services.AccountService
import brs.util.convert.parseAccountId
import brs.util.convert.toUnsignedString
import brs.util.mustGetAsString
import com.google.gson.JsonElement
import com.google.gson.JsonObject


@Deprecated("This call is no longer made by the other peers so will soon be removed.")
class GetAccountBalance @Deprecated("")
internal constructor(private val accountService: AccountService) : PeerServlet.PeerRequestHandler {
    override suspend fun processRequest(request: JsonObject, peer: Peer): JsonElement {

        val response = JsonObject()

        val accountId = request.get(ACCOUNT_ID_PARAMETER_FIELD).mustGetAsString(ACCOUNT_ID_PARAMETER_FIELD).parseAccountId()
        val account = accountService.getAccount(accountId)
        if (account != null) {
            response.addProperty(BALANCE_NQT_RESPONSE_FIELD, account.balanceNQT.toUnsignedString())
        } else {
            response.addProperty(BALANCE_NQT_RESPONSE_FIELD, "0")
        }

        return response
    }

    companion object {

        internal const val ACCOUNT_ID_PARAMETER_FIELD = "account"
        internal const val BALANCE_NQT_RESPONSE_FIELD = "balanceNQT"
    }
}
