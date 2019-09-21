package brs.http

import brs.Attachment
import brs.BurstException
import brs.DependencyProvider
import brs.http.common.Parameters.RECIPIENT_PARAMETER
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class SendMessage(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.MESSAGES, APITag.CREATE_TRANSACTION), RECIPIENT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(request: HttpServletRequest): JsonElement {
        val recipient = ParameterParser.getRecipientId(request)
        val account = dp.parameterService.getSenderAccount(request)
        return createTransaction(request, account, recipient, 0, Attachment.ARBITRARY_MESSAGE)
    }

}
