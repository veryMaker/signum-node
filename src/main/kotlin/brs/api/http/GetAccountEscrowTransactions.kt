package brs.api.http

import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.ESCROWS_RESPONSE
import brs.services.EscrowService
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAccountEscrowTransactions internal constructor(
    private val parameterService: ParameterService,
    private val escrowService: EscrowService
) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), ACCOUNT_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {
        val account = parameterService.getAccount(request) ?: return JSONResponses.INCORRECT_ACCOUNT

        val accountEscrows = escrowService.getEscrowTransactionsByParticipant(account.id)

        val response = JsonObject()

        val escrows = JsonArray()

        for (escrow in accountEscrows) {
            escrows.add(JSONData.escrowTransaction(escrow))
        }

        response.add(ESCROWS_RESPONSE, escrows)
        return response
    }
}
