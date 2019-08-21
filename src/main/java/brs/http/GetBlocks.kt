package brs.http

import brs.Block
import brs.Blockchain
import brs.http.common.Parameters
import brs.services.BlockService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.*

internal class GetBlocks(private val blockchain: Blockchain, private val blockService: BlockService) : APIServlet.JsonRequestHandler(arrayOf(APITag.BLOCKS), FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, INCLUDE_TRANSACTIONS_PARAMETER) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val firstIndex = ParameterParser.getFirstIndex(req)
        var lastIndex = ParameterParser.getLastIndex(req)
        if (lastIndex < 0 || lastIndex - firstIndex > 99) {
            lastIndex = firstIndex + 99
        }

        val includeTransactions = Parameters.isTrue(req.getParameter(Parameters.INCLUDE_TRANSACTIONS_PARAMETER))

        val blocks = JsonArray()
        for (block in blockchain.getBlocks(firstIndex, lastIndex)) {
            blocks.add(JSONData.block(block, includeTransactions, blockchain.height, blockService.getBlockReward(block), blockService.getScoopNum(block)))
        }

        val response = JsonObject()
        response.add("blocks", blocks)

        return response
    }
}
