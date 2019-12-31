package brs.api.http

import brs.api.http.common.Parameters.HEIGHT_PARAMETER
import brs.api.http.common.Parameters.NUM_BLOCKS_PARAMETER
import brs.api.http.common.ResultFields.BLOCKS_RESPONSE
import brs.api.http.common.ResultFields.ERROR_RESPONSE
import brs.services.BlockService
import brs.services.BlockchainProcessorService
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
internal class PopOff(
    private val blockchainProcessorService: BlockchainProcessorService,
    private val blockchainService: BlockchainService,
    private val blockService: BlockService
) : APIServlet.JsonRequestHandler(arrayOf(APITag.DEBUG), NUM_BLOCKS_PARAMETER, HEIGHT_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {

        val response = JsonObject()
        var numBlocks = 0
        try {
            numBlocks = Integer.parseInt(request[NUM_BLOCKS_PARAMETER])
        } catch (ignored: NumberFormatException) {
        }

        var height = 0
        try {
            height = Integer.parseInt(request[HEIGHT_PARAMETER])
        } catch (ignored: NumberFormatException) {
        }

        val blocksJSON = JsonArray()
        val blocks = when {
            numBlocks > 0 -> blockchainProcessorService.popOffTo(blockchainService.height - numBlocks)
            height > 0 -> blockchainProcessorService.popOffTo(height)
            else -> {
                response.addProperty(ERROR_RESPONSE, "invalid numBlocks or height")
                return response
            }
        }
        for (block in blocks) {
            blocksJSON.add(
                JSONData.block(
                    block,
                    true,
                    blockchainService.height,
                    blockService.getBlockReward(block),
                    blockService.getScoopNum(block)
                )
            )
        }
        response.add(BLOCKS_RESPONSE, blocksJSON)
        return response
    }

    override fun requirePost(): Boolean {
        return true
    }

}
