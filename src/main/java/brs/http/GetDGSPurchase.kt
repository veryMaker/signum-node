package brs.http

import brs.BurstException
import brs.http.common.Parameters.PURCHASE_PARAMETER
import brs.services.ParameterService
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class GetDGSPurchase(private val parameterService: ParameterService) : APIServlet.JsonRequestHandler(arrayOf(APITag.DGS), PURCHASE_PARAMETER) {
    internal override fun processRequest(request: HttpServletRequest): JsonElement {
        return JSONData.purchase(parameterService.getPurchase(request))
    }
}
