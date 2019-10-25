package brs.api.http

import brs.transaction.appendix.Attachment
import brs.DependencyProvider
import brs.api.http.common.Parameters.RECIPIENT_PARAMETER
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class SendMessage(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.MESSAGES, APITag.CREATE_TRANSACTION), RECIPIENT_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {
        val recipient = ParameterParser.getRecipientId(request)
        val account = dp.parameterService.getSenderAccount(request)
        return createTransaction(request, account, recipient, 0, Attachment.ArbitraryMessage(dp))
    }

}
