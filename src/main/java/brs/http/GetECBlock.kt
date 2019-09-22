package brs.http

import brs.Blockchain
import brs.BurstException
import brs.EconomicClustering
import brs.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.http.common.ResultFields.EC_BLOCK_HEIGHT_RESPONSE
import brs.http.common.ResultFields.EC_BLOCK_ID_RESPONSE
import brs.http.common.ResultFields.TIMESTAMP_RESPONSE
import brs.services.TimeService
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetECBlock(private val blockchain: Blockchain, private val timeService: TimeService, private val economicClustering: EconomicClustering) : APIServlet.JsonRequestHandler(arrayOf(APITag.BLOCKS), TIMESTAMP_PARAMETER) {
    internal override fun processRequest(request: HttpServletRequest): JsonElement {
        var timestamp = ParameterParser.getTimestamp(request)
        if (timestamp == 0) {
            timestamp = timeService.epochTime
        }
        if (timestamp < blockchain.lastBlock.timestamp - 15) {
            return JSONResponses.INCORRECT_TIMESTAMP
        }
        val ecBlock = economicClustering.getECBlock(timestamp)
        val response = JsonObject()
        response.addProperty(EC_BLOCK_ID_RESPONSE, ecBlock.stringId)
        response.addProperty(EC_BLOCK_HEIGHT_RESPONSE, ecBlock.height)
        response.addProperty(TIMESTAMP_RESPONSE, timestamp)
        return response
    }
}
