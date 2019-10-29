package brs.api.http

import brs.services.BlockchainService
import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.api.http.common.ResultFields.BLOCK_IDS_RESPONSE
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAccountBlockIds internal constructor(
    private val parameterService: ParameterService,
    private val blockchainService: BlockchainService
) : APIServlet.JsonRequestHandler(
    arrayOf(APITag.ACCOUNTS),
    ACCOUNT_PARAMETER,
    TIMESTAMP_PARAMETER,
    FIRST_INDEX_PARAMETER,
    LAST_INDEX_PARAMETER
) {

    override fun processRequest(request: HttpServletRequest): JsonElement {
        val account = parameterService.getAccount(request) ?: return JSONResponses.INCORRECT_ACCOUNT

        val timestamp = ParameterParser.getTimestamp(request)
        val firstIndex = ParameterParser.getFirstIndex(request)
        val lastIndex = ParameterParser.getLastIndex(request)

        val blockIds = JsonArray()
        for (block in blockchainService.getBlocks(account, timestamp, firstIndex, lastIndex)) {
            blockIds.add(block.stringId)
        }

        val response = JsonObject()
        response.add(BLOCK_IDS_RESPONSE, blockIds)

        return response
    }

}
