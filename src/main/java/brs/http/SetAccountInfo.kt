package brs.http

import brs.*
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.INCORRECT_ACCOUNT_DESCRIPTION_LENGTH
import brs.http.JSONResponses.INCORRECT_ACCOUNT_NAME_LENGTH
import brs.http.common.Parameters.DESCRIPTION_PARAMETER
import brs.http.common.Parameters.NAME_PARAMETER

internal class SetAccountInfo(private val parameterService: ParameterService, private val blockchain: Blockchain, apiTransactionManager: APITransactionManager) : CreateTransaction(arrayOf(APITag.ACCOUNTS, APITag.CREATE_TRANSACTION), apiTransactionManager, NAME_PARAMETER, DESCRIPTION_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val name = Convert.nullToEmpty(req.getParameter(NAME_PARAMETER)).trim { it <= ' ' }
        val description = Convert.nullToEmpty(req.getParameter(DESCRIPTION_PARAMETER)).trim { it <= ' ' }

        if (name.length > Constants.MAX_ACCOUNT_NAME_LENGTH) {
            return INCORRECT_ACCOUNT_NAME_LENGTH
        }

        if (description.length > Constants.MAX_ACCOUNT_DESCRIPTION_LENGTH) {
            return INCORRECT_ACCOUNT_DESCRIPTION_LENGTH
        }

        val account = parameterService.getSenderAccount(req)
        val attachment = Attachment.MessagingAccountInfo(name, description, blockchain.height)
        return createTransaction(req, account, attachment)

    }

}
