package brs.api.http

import brs.api.http.JSONResponses.INCORRECT_HEIGHT
import brs.api.http.JSONResponses.MISSING_HEIGHT
import brs.api.http.common.Parameters.HEIGHT_PARAMETER
import brs.services.BlockchainService
import brs.util.convert.emptyToNull
import brs.util.convert.toUnsignedString
import brs.util.jetty.get
import com.google.gson.JsonElement
import brs.util.jetty.get
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetBlockId(private val blockchainService: BlockchainService) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.BLOCKS), HEIGHT_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {

        val height: Int
        try {
            val heightValue = request[HEIGHT_PARAMETER].emptyToNull() ?: return MISSING_HEIGHT
            height = Integer.parseInt(heightValue)
        } catch (e: Exception) {
            return INCORRECT_HEIGHT
        }

        return try {
            val response = JsonObject()
            response.addProperty("block", blockchainService.getBlockIdAtHeight(height).toUnsignedString())
            response
        } catch (e: Exception) {
            INCORRECT_HEIGHT
        }

    }

}
