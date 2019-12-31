package brs.api.http

import brs.api.http.common.Parameters
import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.INCLUDE_TRANSACTIONS_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.api.http.common.ResultFields.BLOCKS_RESPONSE
import brs.api.http.common.JSONData
import brs.services.BlockService
import brs.services.BlockchainService
import brs.services.ParameterService
import com.google.gson.JsonArray
import brs.util.jetty.get
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetAccountBlocks internal constructor(
    private val blockchainService: BlockchainService,
    private val parameterService: ParameterService,
    private val blockService: BlockService
) : APIServlet.JsonRequestHandler(
    arrayOf(APITag.ACCOUNTS),
    ACCOUNT_PARAMETER,
    TIMESTAMP_PARAMETER,
    FIRST_INDEX_PARAMETER,
    LAST_INDEX_PARAMETER,
    INCLUDE_TRANSACTIONS_PARAMETER
) {

    override fun processRequest(request: HttpServletRequest): JsonElement {

        val account = parameterService.getAccount(request)
        val timestamp = ParameterParser.getTimestamp(request)
        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        val includeTransactions = Parameters.isTrue(request[INCLUDE_TRANSACTIONS_PARAMETER])

        val blocks = JsonArray()
        for (block in blockchainService.getBlocks(account, timestamp, firstIndex, lastIndex)) {
            blocks.add(
                JSONData.block(
                    block,
                    includeTransactions,
                    blockchainService.height,
                    blockService.getBlockReward(block),
                    blockService.getScoopNum(block)
                )
            )
        }

        val response = JsonObject()
        response.add(BLOCKS_RESPONSE, blocks)

        return response
    }

}
