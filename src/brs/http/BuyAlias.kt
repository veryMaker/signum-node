package brs.http

import brs.*
import brs.services.AliasService
import brs.services.ParameterService
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

import brs.http.JSONResponses.INCORRECT_ALIAS_NOTFORSALE
import brs.http.common.Parameters.*

internal class BuyAlias(private val parameterService: ParameterService, private val blockchain: Blockchain, private val aliasService: AliasService, apiTransactionManager: APITransactionManager) : CreateTransaction(arrayOf(APITag.ALIASES, APITag.CREATE_TRANSACTION), apiTransactionManager, ALIAS_PARAMETER, ALIAS_NAME_PARAMETER, AMOUNT_NQT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val buyer = parameterService.getSenderAccount(req)
        val alias = parameterService.getAlias(req)
        val amountNQT = ParameterParser.getAmountNQT(req)

        if (aliasService.getOffer(alias) == null) {
            return INCORRECT_ALIAS_NOTFORSALE
        }

        val sellerId = alias.accountId
        val attachment = Attachment.MessagingAliasBuy(alias.aliasName, blockchain.height)
        return createTransaction(req, buyer, sellerId, amountNQT, attachment)
    }
}
