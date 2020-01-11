package brs.api.http

import brs.Burst
import brs.api.http.common.ResultFields.TIME_RESPONSE
import brs.services.BlockchainProcessorService
import brs.services.BlockchainService
import brs.services.TimeService
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetBlockchainStatus(
    private val blockchainProcessorService: BlockchainProcessorService,
    private val blockchainService: BlockchainService,
    private val timeService: TimeService
) : APIServlet.JsonRequestHandler(arrayOf(APITag.BLOCKS, APITag.INFO)) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val response = JsonObject()
        response.addProperty("application", Burst.APPLICATION)
        response.addProperty("version", Burst.VERSION.toString())
        response.addProperty(TIME_RESPONSE, timeService.epochTime)
        val lastBlock = blockchainService.lastBlock
        response.addProperty("lastBlock", lastBlock.stringId)
        response.addProperty("cumulativeDifficulty", lastBlock.cumulativeDifficulty.toString())
        response.addProperty("numberOfBlocks", lastBlock.height + 1)
        val lastBlockchainFeeder = blockchainProcessorService.lastBlockchainFeeder
        response.addProperty("lastBlockchainFeeder", lastBlockchainFeeder?.address?.toString())
        response.addProperty("lastBlockchainFeederHeight", blockchainProcessorService.lastBlockchainFeederHeight)
        return response
    }
}
