package brs.api.http

import brs.api.http.common.Parameters.ALIAS_NAME_PARAMETER
import brs.api.http.common.Parameters.ALIAS_PARAMETER
import brs.services.AliasService
import brs.services.ParameterService
import brs.util.jetty.get
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetAlias internal constructor(
    private val parameterService: ParameterService,
    private val aliasService: AliasService
) : APIServlet.JsonRequestHandler(arrayOf(APITag.ALIASES), ALIAS_PARAMETER, ALIAS_NAME_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val alias = parameterService.getAlias(request)
        val offer = aliasService.getOffer(alias)
        return JSONData.alias(alias, offer)
    }
}
