package brs.http

import brs.Blockchain
import brs.http.JSONResponses.INCORRECT_BLOCK
import brs.http.JSONResponses.INCORRECT_HEIGHT
import brs.http.JSONResponses.INCORRECT_TIMESTAMP
import brs.http.JSONResponses.UNKNOWN_BLOCK
import brs.http.common.Parameters.BLOCK_PARAMETER
import brs.http.common.Parameters.HEIGHT_PARAMETER
import brs.http.common.Parameters.INCLUDE_TRANSACTIONS_PARAMETER
import brs.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.http.common.Parameters.isTrue
import brs.services.BlockService
import brs.util.convert.emptyToNull
import brs.util.convert.parseUnsignedLong
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

internal class GetBlock internal constructor(private val blockchain: Blockchain, private val blockService: BlockService) : APIServlet.JsonRequestHandler(arrayOf(APITag.BLOCKS), BLOCK_PARAMETER, HEIGHT_PARAMETER, TIMESTAMP_PARAMETER, INCLUDE_TRANSACTIONS_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {
        val blockValue = request.getParameter(BLOCK_PARAMETER).emptyToNull()
        val heightValue = request.getParameter(HEIGHT_PARAMETER).emptyToNull()
        val timestampValue = request.getParameter(TIMESTAMP_PARAMETER).emptyToNull()

        val blockData = when {
            blockValue != null -> try {
                blockchain.getBlock(blockValue.parseUnsignedLong())
            } catch (e: RuntimeException) {
                return INCORRECT_BLOCK
            }
            heightValue != null -> try {
                val height = Integer.parseInt(heightValue)
                if (height < 0 || height > blockchain.height) {
                    return INCORRECT_HEIGHT
                }
                blockchain.getBlockAtHeight(height)
            } catch (e: RuntimeException) {
                return INCORRECT_HEIGHT
            }
            timestampValue != null -> try {
                val timestamp = Integer.parseInt(timestampValue)
                if (timestamp < 0) {
                    return INCORRECT_TIMESTAMP
                }
                blockchain.getLastBlock(timestamp)
            } catch (e: RuntimeException) {
                return INCORRECT_TIMESTAMP
            }
            else -> blockchain.lastBlock
        } ?: return UNKNOWN_BLOCK

        val includeTransactions = isTrue(request.getParameter(INCLUDE_TRANSACTIONS_PARAMETER))

        return JSONData.block(blockData, includeTransactions, blockchain.height, blockService.getBlockReward(blockData), blockService.getScoopNum(blockData))
    }
}
