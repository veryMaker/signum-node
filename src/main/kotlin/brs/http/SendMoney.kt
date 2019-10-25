package brs.http

import brs.DependencyProvider
import brs.http.common.Parameters.AMOUNT_PLANCK_PARAMETER
import brs.http.common.Parameters.RECIPIENT_PARAMETER
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class SendMoney(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.ACCOUNTS, APITag.CREATE_TRANSACTION), RECIPIENT_PARAMETER, AMOUNT_PLANCK_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val recipient = ParameterParser.getRecipientId(request)
        val amountPlanck = ParameterParser.getAmountPlanck(request)
        val account = dp.parameterService.getSenderAccount(request)
        return createTransaction(request, account, recipient, amountPlanck)
    }
}
