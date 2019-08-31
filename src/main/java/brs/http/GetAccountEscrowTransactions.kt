package brs.http

import brs.Account
import brs.BurstException
import brs.Escrow
import brs.services.EscrowService
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.ESCROWS_RESPONSE

internal class GetAccountEscrowTransactions internal constructor(private val parameterService: ParameterService, private val escrowService: EscrowService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), ACCOUNT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val account = parameterService.getAccount(req)

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
