package brs.http

import brs.*
import brs.services.AliasService
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.INCORRECT_ALIAS_NOTFORSALE
import brs.http.common.Parameters.*

internal class BuyAlias(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.ALIASES, APITag.CREATE_TRANSACTION), ALIAS_PARAMETER, ALIAS_NAME_PARAMETER, AMOUNT_NQT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val buyer = dp.parameterService.getSenderAccount(req)
        val alias = dp.parameterService.getAlias(req)
        val amountNQT = ParameterParser.getAmountNQT(req)

        if (dp.aliasService.getOffer(alias) == null) {
            return INCORRECT_ALIAS_NOTFORSALE
        }

        val sellerId = alias.accountId
        val attachment = Attachment.MessagingAliasBuy(alias.aliasName, dp.blockchain.height)
        return createTransaction(req, buyer, sellerId, amountNQT, attachment)
    }
}
