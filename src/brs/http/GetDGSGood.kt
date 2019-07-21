package brs.http

import brs.BurstException
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.GOODS_PARAMETER

internal class GetDGSGood internal constructor(private val parameterService: ParameterService) : APIServlet.JsonRequestHandler(arrayOf(APITag.DGS), GOODS_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        return JSONData.goods(parameterService.getGoods(req))
    }

}
