package brs.api.http

import brs.api.http.common.JSONData
import brs.api.http.common.JSONResponses.INCORRECT_BLOCK
import brs.api.http.common.JSONResponses.INCORRECT_HEIGHT
import brs.api.http.common.JSONResponses.INCORRECT_TIMESTAMP
import brs.api.http.common.JSONResponses.UNKNOWN_BLOCK
import brs.api.http.common.Parameters.BLOCK_PARAMETER
import brs.api.http.common.Parameters.HEIGHT_PARAMETER
import brs.api.http.common.Parameters.INCLUDE_TRANSACTIONS_PARAMETER
import brs.api.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.api.http.common.Parameters.isTrue
import brs.services.BlockService
import brs.services.BlockchainService
import brs.util.convert.emptyToNull
import brs.util.convert.parseUnsignedLong
import brs.util.jetty.get
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetBlock internal constructor(
    private val blockchainService: BlockchainService,
    private val blockService: BlockService
) : APIServlet.JsonRequestHandler(
    arrayOf(APITag.BLOCKS),
    BLOCK_PARAMETER,
    HEIGHT_PARAMETER,
    TIMESTAMP_PARAMETER,
    INCLUDE_TRANSACTIONS_PARAMETER
) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val blockValue = request[BLOCK_PARAMETER].emptyToNull()
        val heightValue = request[HEIGHT_PARAMETER].emptyToNull()
        val timestampValue = request[TIMESTAMP_PARAMETER].emptyToNull()

        val blockData = when {
            blockValue != null -> try {
                blockchainService.getBlock(blockValue.parseUnsignedLong())
            } catch (e: Exception) {
                return INCORRECT_BLOCK
            }
            heightValue != null -> try {
                val height = Integer.parseInt(heightValue)
                if (height < 0 || height > blockchainService.height) {
                    return INCORRECT_HEIGHT
                }
                blockchainService.getBlockAtHeight(height)
            } catch (e: Exception) {
                return INCORRECT_HEIGHT
            }
            timestampValue != null -> try {
                val timestamp = Integer.parseInt(timestampValue)
                if (timestamp < 0) {
                    return INCORRECT_TIMESTAMP
                }
                blockchainService.getLastBlock(timestamp)
            } catch (e: Exception) {
                return INCORRECT_TIMESTAMP
            }
            else -> blockchainService.lastBlock
        } ?: return UNKNOWN_BLOCK

        val includeTransactions = isTrue(request[INCLUDE_TRANSACTIONS_PARAMETER])

        return JSONData.block(
            blockData,
            includeTransactions,
            blockchainService.height,
            blockService.getBlockReward(blockData),
            blockService.getScoopNum(blockData)
        )
    }
}
