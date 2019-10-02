package brs.http

import brs.*
import brs.http.JSONResponses.INCORRECT_ALIAS_LENGTH
import brs.http.JSONResponses.INCORRECT_ALIAS_NAME
import brs.http.JSONResponses.INCORRECT_URI_LENGTH
import brs.http.JSONResponses.MISSING_ALIAS_NAME
import brs.services.AliasService
import brs.services.ParameterService
import brs.util.Convert
import brs.util.TextUtils
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest


import brs.http.common.Parameters.ALIAS_NAME_PARAMETER
import brs.http.common.Parameters.ALIAS_URI_PARAMETER
import brs.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE

internal class SetAlias(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.ALIASES, APITag.CREATE_TRANSACTION), ALIAS_NAME_PARAMETER, ALIAS_URI_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        var aliasName = Convert.emptyToNull(request.getParameter(ALIAS_NAME_PARAMETER))
        var aliasURI = Convert.nullToEmpty(request.getParameter(ALIAS_URI_PARAMETER))

        if (aliasName == null) {
            return MISSING_ALIAS_NAME
        }

        aliasName = aliasName.trim { it <= ' ' }
        if (aliasName.isEmpty() || aliasName.length > Constants.MAX_ALIAS_LENGTH) {
            return INCORRECT_ALIAS_LENGTH
        }

        if (!TextUtils.isInAlphabet(aliasName)) {
            return INCORRECT_ALIAS_NAME
        }

        aliasURI = aliasURI.trim { it <= ' ' }
        if (aliasURI.length > Constants.MAX_ALIAS_URI_LENGTH) {
            return INCORRECT_URI_LENGTH
        }

        val account = dp.parameterService.getSenderAccount(request)

        val alias = dp.aliasService.getAlias(aliasName)
        if (alias != null && alias.accountId != account.id) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 8)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "\"$aliasName\" is already used")
            return response
        }

        val attachment = Attachment.MessagingAliasAssignment(dp, aliasName, aliasURI, dp.blockchain.height)
        return createTransaction(request, account, attachment)

    }

}
