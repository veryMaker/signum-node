package brs.http

import brs.Block
import brs.Blockchain
import brs.BlockchainProcessor
import brs.http.common.Parameters.HEIGHT_PARAMETER
import brs.http.common.Parameters.NUM_BLOCKS_PARAMETER
import brs.http.common.ResultFields.BLOCKS_RESPONSE
import brs.http.common.ResultFields.ERROR_RESPONSE
import brs.services.BlockService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class PopOff(private val blockchainProcessor: BlockchainProcessor, private val blockchain: Blockchain, private val blockService: BlockService) : APIServlet.JsonRequestHandler(arrayOf(APITag.DEBUG), NUM_BLOCKS_PARAMETER, HEIGHT_PARAMETER) {

    internal override fun processRequest(request: HttpServletRequest): JsonElement {

        val response = JsonObject()
        var numBlocks = 0
        try {
            numBlocks = Integer.parseInt(request.getParameter(NUM_BLOCKS_PARAMETER))
        } catch (ignored: NumberFormatException) {
        }

        var height = 0
        try {
            height = Integer.parseInt(request.getParameter(HEIGHT_PARAMETER))
        } catch (ignored: NumberFormatException) {
        }

        val blocks: List<Block>
        val blocksJSON = JsonArray()
        if (numBlocks > 0) {
            blocks = blockchainProcessor.popOffTo(blockchain.height - numBlocks)
        } else if (height > 0) {
            blocks = blockchainProcessor.popOffTo(height)
        } else {
            response.addProperty(ERROR_RESPONSE, "invalid numBlocks or height")
            return response
        }
        for (block in blocks) {
            blocksJSON.add(JSONData.block(block, true, blockchain.height, blockService.getBlockReward(block), blockService.getScoopNum(block)))
        }
        response.add(BLOCKS_RESPONSE, blocksJSON)
        return response
    }

    internal override fun requirePost(): Boolean {
        return true
    }

}
