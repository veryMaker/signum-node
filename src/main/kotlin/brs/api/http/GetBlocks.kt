package brs.api.http

import brs.api.http.common.Parameters
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.INCLUDE_TRANSACTIONS_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.services.BlockService
import brs.services.BlockchainService
import com.google.gson.JsonArray
import brs.util.jetty.get
import com.google.gson.JsonElement
import brs.util.jetty.get
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetBlocks(private val blockchainService: BlockchainService, private val blockService: BlockService) :
    APIServlet.JsonRequestHandler(
        arrayOf(APITag.BLOCKS),
        FIRST_INDEX_PARAMETER,
        LAST_INDEX_PARAMETER,
        INCLUDE_TRANSACTIONS_PARAMETER
    ) {

    override fun processRequest(request: HttpServletRequest): JsonElement {

        val firstIndex = ParameterParser.getFirstIndex(request)
        var lastIndex = ParameterParser.getLastIndex(request)
        if (lastIndex < 0 || lastIndex - firstIndex > 99) {
            lastIndex = firstIndex + 99
        }

        val includeTransactions = Parameters.isTrue(request[INCLUDE_TRANSACTIONS_PARAMETER])

        val blocks = JsonArray()
        for (block in blockchainService.getBlocks(firstIndex, lastIndex)) {
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
        response.add("blocks", blocks)

        return response
    }
}
