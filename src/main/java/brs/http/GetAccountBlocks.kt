package brs.http

import brs.Blockchain
import brs.http.common.Parameters
import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.INCLUDE_TRANSACTIONS_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.http.common.ResultFields.BLOCKS_RESPONSE
import brs.services.BlockService
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAccountBlocks internal constructor(private val blockchain: Blockchain, private val parameterService: ParameterService, private val blockService: BlockService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), ACCOUNT_PARAMETER, TIMESTAMP_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, INCLUDE_TRANSACTIONS_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {

        val account = parameterService.getAccount(request) ?: return JSONResponses.INCORRECT_ACCOUNT
        val timestamp = ParameterParser.getTimestamp(request)
        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        val includeTransactions = Parameters.isTrue(request.getParameter(INCLUDE_TRANSACTIONS_PARAMETER))

        val blocks = JsonArray()
        for (block in blockchain.getBlocks(account, timestamp, firstIndex, lastIndex)) {
            blocks.add(JSONData.block(block, includeTransactions, blockchain.height, blockService.getBlockReward(block), blockService.getScoopNum(block)))
        }

        val response = JsonObject()
        response.add(BLOCKS_RESPONSE, blocks)

        return response
    }

}
