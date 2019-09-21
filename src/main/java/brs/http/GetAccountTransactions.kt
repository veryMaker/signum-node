package brs.http

import brs.Blockchain
import brs.BurstException
import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.INCLUDE_INDIRECT_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.Parameters.NUMBER_OF_CONFIRMATIONS_PARAMETER
import brs.http.common.Parameters.SUBTYPE_PARAMETER
import brs.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.http.common.Parameters.TYPE_PARAMETER
import brs.http.common.ResultFields.TRANSACTIONS_RESPONSE
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAccountTransactions(private val parameterService: ParameterService, private val blockchain: Blockchain) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), ACCOUNT_PARAMETER, TIMESTAMP_PARAMETER, TYPE_PARAMETER, SUBTYPE_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, NUMBER_OF_CONFIRMATIONS_PARAMETER, INCLUDE_INDIRECT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(request: HttpServletRequest): JsonElement {
        val account = parameterService.getAccount(request)
        val timestamp = ParameterParser.getTimestamp(request)
        val numberOfConfirmations = parameterService.getNumberOfConfirmations(request)

        val type: Byte = try {
            java.lang.Byte.parseByte(request.getParameter(TYPE_PARAMETER))
        } catch (e: NumberFormatException) {
            -1
        }
        val subtype: Byte = try {
            java.lang.Byte.parseByte(request.getParameter(SUBTYPE_PARAMETER))
        } catch (e: NumberFormatException) {
            -1
        }

        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        require(lastIndex >= firstIndex) { "lastIndex must be greater or equal to firstIndex" }

        val transactions = JsonArray()
        for (transaction in blockchain.getTransactions(account, numberOfConfirmations, type, subtype, timestamp, firstIndex, lastIndex, parameterService.getIncludeIndirect(request))) {
            transactions.add(JSONData.transaction(transaction, blockchain.height))
        }

        val response = JsonObject()
        response.add(TRANSACTIONS_RESPONSE, transactions)
        return response
    }
}
