package brs.http

import brs.BurstException
import brs.DependencyProvider
import brs.http.common.Parameters.AMOUNT_NQT_PARAMETER
import brs.http.common.Parameters.RECIPIENT_PARAMETER
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class SendMoney(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.ACCOUNTS, APITag.CREATE_TRANSACTION), RECIPIENT_PARAMETER, AMOUNT_NQT_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        val recipient = ParameterParser.getRecipientId(request)
        val amountNQT = ParameterParser.getAmountNQT(request)
        val account = dp.parameterService.getSenderAccount(request)
        return createTransaction(request, account, recipient, amountNQT)
    }

}
