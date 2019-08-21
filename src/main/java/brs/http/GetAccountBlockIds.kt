package brs.http

import brs.Account
import brs.Block
import brs.Blockchain
import brs.BurstException
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.*
import brs.http.common.ResultFields.BLOCK_IDS_RESPONSE

internal class GetAccountBlockIds internal constructor(private val parameterService: ParameterService, private val blockchain: Blockchain) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), ACCOUNT_PARAMETER, TIMESTAMP_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val account = parameterService.getAccount(req)

        val timestamp = ParameterParser.getTimestamp(req)
        val firstIndex = ParameterParser.getFirstIndex(req)
        val lastIndex = ParameterParser.getLastIndex(req)

        val blockIds = JsonArray()
        for (block in blockchain.getBlocks(account, timestamp, firstIndex, lastIndex)) {
            blockIds.add(block.stringId)
        }

        val response = JsonObject()
        response.add(BLOCK_IDS_RESPONSE, blockIds)

        return response
    }

}
