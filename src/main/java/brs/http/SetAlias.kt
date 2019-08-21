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

internal class SetAlias(private val parameterService: ParameterService, private val blockchain: Blockchain, private val aliasService: AliasService, apiTransactionManager: APITransactionManager) : CreateTransaction(arrayOf(APITag.ALIASES, APITag.CREATE_TRANSACTION), apiTransactionManager, ALIAS_NAME_PARAMETER, ALIAS_URI_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        var aliasName = Convert.emptyToNull(req.getParameter(ALIAS_NAME_PARAMETER))
        var aliasURI = Convert.nullToEmpty(req.getParameter(ALIAS_URI_PARAMETER))

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

        val account = parameterService.getSenderAccount(req)

        val alias = aliasService.getAlias(aliasName)
        if (alias != null && alias.accountId != account.getId()) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 8)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "\"$aliasName\" is already used")
            return response
        }

        val attachment = Attachment.MessagingAliasAssignment(aliasName, aliasURI, blockchain.height)
        return createTransaction(req, account, attachment)

    }

}
