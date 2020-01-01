package brs.api.http

import brs.api.http.common.Parameters.RECIPIENT_PARAMETER
import brs.entity.DependencyProvider
import brs.transaction.appendix.Attachment
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class SendMessage(private val dp: DependencyProvider) :
    CreateTransaction(dp, arrayOf(APITag.MESSAGES, APITag.CREATE_TRANSACTION), RECIPIENT_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val recipient = ParameterParser.getRecipientId(request)
        val account = dp.parameterService.getSenderAccount(request)
        return createTransaction(request, account, recipient, 0, Attachment.ArbitraryMessage(dp))
    }
}
