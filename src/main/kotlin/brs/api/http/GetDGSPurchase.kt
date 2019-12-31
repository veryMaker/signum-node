package brs.api.http

import brs.api.http.common.Parameters.PURCHASE_PARAMETER
import brs.services.ParameterService
import brs.util.jetty.get
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetDGSPurchase(private val parameterService: ParameterService) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.DGS), PURCHASE_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        return JSONData.purchase(parameterService.getPurchase(request))
    }
}
