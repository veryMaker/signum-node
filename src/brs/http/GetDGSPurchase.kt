package brs.http

import brs.BurstException
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.PURCHASE_PARAMETER

internal class GetDGSPurchase(private val parameterService: ParameterService) : APIServlet.JsonRequestHandler(arrayOf(APITag.DGS), PURCHASE_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        return JSONData.purchase(parameterService.getPurchase(req))
    }

}
