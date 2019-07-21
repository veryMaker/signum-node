package brs.http

import brs.Alias
import brs.Alias.Offer
import brs.services.AliasService
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.ALIAS_NAME_PARAMETER
import brs.http.common.Parameters.ALIAS_PARAMETER

internal class GetAlias internal constructor(private val parameterService: ParameterService, private val aliasService: AliasService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ALIASES), ALIAS_PARAMETER, ALIAS_NAME_PARAMETER) {

    @Throws(ParameterException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val alias = parameterService.getAlias(req)
        val offer = aliasService.getOffer(alias)

        return JSONData.alias(alias, offer)
    }

}
