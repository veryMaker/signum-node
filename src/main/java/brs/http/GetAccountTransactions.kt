package brs.http

import brs.Account
import brs.Blockchain
import brs.BurstException
import brs.Transaction
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.*
import brs.http.common.ResultFields.TRANSACTIONS_RESPONSE

internal class GetAccountTransactions(private val parameterService: ParameterService, private val blockchain: Blockchain) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), ACCOUNT_PARAMETER, TIMESTAMP_PARAMETER, TYPE_PARAMETER, SUBTYPE_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, NUMBER_OF_CONFIRMATIONS_PARAMETER, INCLUDE_INDIRECT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val account = parameterService.getAccount(req)
        val timestamp = ParameterParser.getTimestamp(req)
        val numberOfConfirmations = parameterService.getNumberOfConfirmations(req)

        var type: Byte
        var subtype: Byte
        try {
            type = java.lang.Byte.parseByte(req.getParameter(TYPE_PARAMETER))
        } catch (e: NumberFormatException) {
            type = -1
        }

        try {
            subtype = java.lang.Byte.parseByte(req.getParameter(SUBTYPE_PARAMETER))
        } catch (e: NumberFormatException) {
            subtype = -1
        }

        val firstIndex = ParameterParser.getFirstIndex(req)
        val lastIndex = ParameterParser.getLastIndex(req)

        if (lastIndex < firstIndex) {
            throw IllegalArgumentException("lastIndex must be greater or equal to firstIndex")
        }

        val transactions = JsonArray()
        for (transaction in blockchain.getTransactions(account, numberOfConfirmations, type, subtype, timestamp, firstIndex, lastIndex, parameterService.getIncludeIndirect(req))) {
            transactions.add(JSONData.transaction(transaction, blockchain.height))
        }

        val response = JsonObject()
        response.add(TRANSACTIONS_RESPONSE, transactions)
        return response
    }
}
