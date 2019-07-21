package brs.http

import brs.Block
import brs.Blockchain
import brs.http.JSONResponses.INCORRECT_BLOCK
import brs.http.JSONResponses.INCORRECT_HEIGHT
import brs.http.JSONResponses.INCORRECT_TIMESTAMP
import brs.http.JSONResponses.UNKNOWN_BLOCK
import brs.http.common.Parameters
import brs.services.BlockService
import brs.util.Convert
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest


import brs.http.common.Parameters.*

internal class GetBlock internal constructor(private val blockchain: Blockchain, private val blockService: BlockService) : APIServlet.JsonRequestHandler(arrayOf(APITag.BLOCKS), BLOCK_PARAMETER, HEIGHT_PARAMETER, TIMESTAMP_PARAMETER, INCLUDE_TRANSACTIONS_PARAMETER) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val blockValue = Convert.emptyToNull(req.getParameter(BLOCK_PARAMETER))
        val heightValue = Convert.emptyToNull(req.getParameter(HEIGHT_PARAMETER))
        val timestampValue = Convert.emptyToNull(req.getParameter(TIMESTAMP_PARAMETER))

        val blockData: Block?
        when {
            blockValue != null -> try {
                blockData = blockchain.getBlock(Convert.parseUnsignedLong(blockValue))
            } catch (e: RuntimeException) {
                return INCORRECT_BLOCK
            }
            heightValue != null -> try {
                val height = Integer.parseInt(heightValue)
                if (height < 0 || height > blockchain.height) {
                    return INCORRECT_HEIGHT
                }
                blockData = blockchain.getBlockAtHeight(height)
            } catch (e: RuntimeException) {
                return INCORRECT_HEIGHT
            }
            timestampValue != null -> try {
                val timestamp = Integer.parseInt(timestampValue)
                if (timestamp < 0) {
                    return INCORRECT_TIMESTAMP
                }
                blockData = blockchain.getLastBlock(timestamp)
            } catch (e: RuntimeException) {
                return INCORRECT_TIMESTAMP
            }
            else -> blockData = blockchain.lastBlock
        }

        val includeTransactions = isTrue(req.getParameter(INCLUDE_TRANSACTIONS_PARAMETER))

        return JSONData.block(blockData, includeTransactions, blockchain.height, blockService.getBlockReward(blockData), blockService.getScoopNum(blockData))
    }
}
