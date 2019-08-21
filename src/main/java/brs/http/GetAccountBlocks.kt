package brs.http

import brs.Account
import brs.Block
import brs.Blockchain
import brs.BurstException
import brs.http.common.Parameters
import brs.services.BlockService
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.*
import brs.http.common.ResultFields.BLOCKS_RESPONSE

internal class GetAccountBlocks internal constructor(private val blockchain: Blockchain, private val parameterService: ParameterService, private val blockService: BlockService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), ACCOUNT_PARAMETER, TIMESTAMP_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, INCLUDE_TRANSACTIONS_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val account = parameterService.getAccount(req)
        val timestamp = ParameterParser.getTimestamp(req)
        val firstIndex = ParameterParser.getFirstIndex(req)
        val lastIndex = ParameterParser.getLastIndex(req)

        val includeTransactions = Parameters.isTrue(req.getParameter(INCLUDE_TRANSACTIONS_PARAMETER))

        val blocks = JsonArray()
        for (block in blockchain.getBlocks(account, timestamp, firstIndex, lastIndex)) {
            blocks.add(JSONData.block(block, includeTransactions, blockchain.height, blockService.getBlockReward(block), blockService.getScoopNum(block)))
        }

        val response = JsonObject()
        response.add(BLOCKS_RESPONSE, blocks)

        return response
    }

}
