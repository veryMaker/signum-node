package brs.http

import brs.Attachment
import brs.BurstException
import brs.DependencyProvider
import brs.http.JSONResponses.INCORRECT_ALIAS_NOTFORSALE
import brs.http.common.Parameters.ALIAS_NAME_PARAMETER
import brs.http.common.Parameters.ALIAS_PARAMETER
import brs.http.common.Parameters.AMOUNT_NQT_PARAMETER
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class BuyAlias(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.ALIASES, APITag.CREATE_TRANSACTION), ALIAS_PARAMETER, ALIAS_NAME_PARAMETER, AMOUNT_NQT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(request: HttpServletRequest): JsonElement {
        val buyer = dp.parameterService.getSenderAccount(request)
        val alias = dp.parameterService.getAlias(request)
        val amountNQT = ParameterParser.getAmountNQT(request)

        if (dp.aliasService.getOffer(alias) == null) {
            return INCORRECT_ALIAS_NOTFORSALE
        }

        val sellerId = alias.accountId
        val attachment = Attachment.MessagingAliasBuy(dp, alias.aliasName, dp.blockchain.height)
        return createTransaction(request, buyer, sellerId, amountNQT, attachment)
    }
}
