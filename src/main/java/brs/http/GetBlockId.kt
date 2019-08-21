package brs.http

import brs.Blockchain
import brs.util.Convert
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.INCORRECT_HEIGHT
import brs.http.JSONResponses.MISSING_HEIGHT
import brs.http.common.Parameters.HEIGHT_PARAMETER

internal class GetBlockId(private val blockchain: Blockchain) : APIServlet.JsonRequestHandler(arrayOf(APITag.BLOCKS), HEIGHT_PARAMETER) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val height: Int
        try {
            val heightValue = Convert.emptyToNull(req.getParameter(HEIGHT_PARAMETER)) ?: return MISSING_HEIGHT
            height = Integer.parseInt(heightValue)
        } catch (e: RuntimeException) {
            return INCORRECT_HEIGHT
        }

        try {
            val response = JsonObject()
            response.addProperty("block", Convert.toUnsignedLong(blockchain.getBlockIdAtHeight(height)))
            return response
        } catch (e: RuntimeException) {
            return INCORRECT_HEIGHT
        }

    }

}
