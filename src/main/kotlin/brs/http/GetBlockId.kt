package brs.http

import brs.Blockchain
import brs.http.JSONResponses.INCORRECT_HEIGHT
import brs.http.JSONResponses.MISSING_HEIGHT
import brs.http.common.Parameters.HEIGHT_PARAMETER
import brs.util.convert.emptyToNull
import brs.util.convert.toUnsignedString
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetBlockId(private val blockchain: Blockchain) : APIServlet.JsonRequestHandler(arrayOf(APITag.BLOCKS), HEIGHT_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {

        val height: Int
        try {
            val heightValue = request.getParameter(HEIGHT_PARAMETER).emptyToNull() ?: return MISSING_HEIGHT
            height = Integer.parseInt(heightValue)
        } catch (e: RuntimeException) {
            return INCORRECT_HEIGHT
        }

        return try {
            val response = JsonObject()
            response.addProperty("block", blockchain.getBlockIdAtHeight(height).toUnsignedString())
            response
        } catch (e: RuntimeException) {
            INCORRECT_HEIGHT
        }

    }

}
