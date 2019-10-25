package brs.api.http

import brs.transaction.appendix.Attachment
import brs.DependencyProvider
import brs.api.http.JSONResponses.INCORRECT_ALIAS_NOTFORSALE
import brs.api.http.common.Parameters.ALIAS_NAME_PARAMETER
import brs.api.http.common.Parameters.ALIAS_PARAMETER
import brs.api.http.common.Parameters.AMOUNT_PLANCK_PARAMETER
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class BuyAlias(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.ALIASES, APITag.CREATE_TRANSACTION), ALIAS_PARAMETER, ALIAS_NAME_PARAMETER, AMOUNT_PLANCK_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val buyer = dp.parameterService.getSenderAccount(request)
        val alias = dp.parameterService.getAlias(request)
        val amountPlanck = ParameterParser.getAmountPlanck(request)

        if (dp.aliasService.getOffer(alias) == null) {
            return INCORRECT_ALIAS_NOTFORSALE
        }

        val sellerId = alias.accountId
        val attachment = Attachment.MessagingAliasBuy(dp, alias.aliasName, dp.blockchainService.height)
        return createTransaction(request, buyer, sellerId, amountPlanck, attachment)
    }
}
