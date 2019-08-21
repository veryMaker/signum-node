package brs.http

import brs.Account
import brs.Blockchain
import brs.BurstException
import brs.services.ParameterService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.Parameters.HEIGHT_PARAMETER
import brs.http.common.ResultFields.*


@Deprecated("This call does nothing. It always returns an empty array.")
internal class GetAccountLessors internal constructor(private val parameterService: ParameterService, private val blockchain: Blockchain) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), ACCOUNT_PARAMETER, HEIGHT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val account = parameterService.getAccount(req)
        var height = parameterService.getHeight(req)
        if (height < 0) {
            height = blockchain.height
        }

        val response = JsonObject()
        JSONData.putAccount(response, ACCOUNT_RESPONSE, account.getId())
        response.addProperty(HEIGHT_RESPONSE, height)
        val lessorsJSON = JsonArray()

        response.add(LESSORS_RESPONSE, lessorsJSON)
        return response
    }
}
