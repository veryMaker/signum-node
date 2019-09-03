package brs.http

import brs.BurstException
import brs.services.AliasService
import brs.services.ParameterService
import brs.util.FilteringIterator
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest
import brs.http.common.ResultFields.ALIASES_RESPONSE

internal class GetAliases internal constructor(private val parameterService: ParameterService, private val aliasService: AliasService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ALIASES), TIMESTAMP_PARAMETER, ACCOUNT_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER) {
    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val timestamp = ParameterParser.getTimestamp(req)
        val accountId = parameterService.getAccount(req).id
        val firstIndex = ParameterParser.getFirstIndex(req)
        val lastIndex = ParameterParser.getLastIndex(req)

        val aliases = JsonArray()
        val aliasIterator = FilteringIterator(aliasService.getAliasesByOwner(accountId, 0, -1),  { alias -> alias != null && alias.timestamp >= timestamp }, firstIndex, lastIndex)
        while (aliasIterator.hasNext()) {
            val alias = aliasIterator.next()
            val offer = aliasService.getOffer(alias)
            aliases.add(JSONData.alias(alias, offer))
        }

        val response = JsonObject()
        response.add(ALIASES_RESPONSE, aliases)
        return response
    }
}
