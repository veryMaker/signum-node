package brs.http

import brs.*
import brs.http.JSONResponses.INCORRECT_ALIAS_OWNER
import brs.http.JSONResponses.INCORRECT_PRICE
import brs.http.JSONResponses.INCORRECT_RECIPIENT
import brs.http.JSONResponses.MISSING_PRICE
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

internal class SellAlias internal constructor(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.ALIASES, APITag.CREATE_TRANSACTION), ALIAS_PARAMETER, ALIAS_NAME_PARAMETER, RECIPIENT_PARAMETER, PRICE_NQT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        val alias = dp.parameterService.getAlias(req)
        val owner = dp.parameterService.getSenderAccount(req)

        val priceValueNQT = Convert.emptyToNull(req.getParameter(PRICE_NQT_PARAMETER)) ?: return MISSING_PRICE
        val priceNQT: Long
        try {
            priceNQT = java.lang.Long.parseLong(priceValueNQT)
        } catch (e: RuntimeException) {
            return INCORRECT_PRICE
        }

        if (priceNQT < 0 || priceNQT > Constants.MAX_BALANCE_NQT) {
            throw ParameterException(INCORRECT_PRICE)
        }

        val recipientValue = Convert.emptyToNull(req.getParameter(RECIPIENT_PARAMETER))
        var recipientId: Long = 0
        if (recipientValue != null) {
            try {
                recipientId = Convert.parseAccountId(recipientValue)
            } catch (e: RuntimeException) {
                return INCORRECT_RECIPIENT
            }

            if (recipientId == 0L) {
                return INCORRECT_RECIPIENT
            }
        }

        if (alias.accountId != owner.id) {
            return INCORRECT_ALIAS_OWNER
        }

        val attachment = Attachment.MessagingAliasSell(alias.aliasName, priceNQT, dp.blockchain.height)
        return createTransaction(req, owner, recipientId, 0, attachment)
    }
}
