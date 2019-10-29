package brs.api.http

import brs.api.http.common.Parameters.GOODS_PARAMETER
import brs.services.ParameterService
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class GetDGSGood internal constructor(private val parameterService: ParameterService) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.DGS), GOODS_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {
        return JSONData.goods(parameterService.getGoods(request))
    }

}
