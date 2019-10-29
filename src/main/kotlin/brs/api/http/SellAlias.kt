package brs.api.http

import brs.transaction.appendix.Attachment
import brs.objects.Constants
import brs.entity.DependencyProvider
import brs.api.http.JSONResponses.INCORRECT_ALIAS_OWNER
import brs.api.http.JSONResponses.INCORRECT_PRICE
import brs.api.http.JSONResponses.INCORRECT_RECIPIENT
import brs.api.http.JSONResponses.MISSING_PRICE
import brs.api.http.common.Parameters.ALIAS_NAME_PARAMETER
import brs.api.http.common.Parameters.ALIAS_PARAMETER
import brs.api.http.common.Parameters.PRICE_PLANCK_PARAMETER
import brs.api.http.common.Parameters.RECIPIENT_PARAMETER
import brs.util.convert.emptyToNull
import brs.util.convert.parseAccountId
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class SellAlias internal constructor(private val dp: DependencyProvider) : CreateTransaction(
    dp,
    arrayOf(APITag.ALIASES, APITag.CREATE_TRANSACTION),
    ALIAS_PARAMETER,
    ALIAS_NAME_PARAMETER,
    RECIPIENT_PARAMETER,
    PRICE_PLANCK_PARAMETER
) {

    override fun processRequest(request: HttpServletRequest): JsonElement {
        val alias = dp.parameterService.getAlias(request)
        val owner = dp.parameterService.getSenderAccount(request)

        val priceValuePlanck = request.getParameter(PRICE_PLANCK_PARAMETER).emptyToNull() ?: return MISSING_PRICE
        val pricePlanck: Long
        try {
            pricePlanck = priceValuePlanck.toLong()
        } catch (e: RuntimeException) {
            return INCORRECT_PRICE
        }

        if (pricePlanck < 0 || pricePlanck > Constants.MAX_BALANCE_PLANCK) {
            throw ParameterException(INCORRECT_PRICE)
        }

        val recipientValue = request.getParameter(RECIPIENT_PARAMETER).emptyToNull()
        var recipientId: Long = 0
        if (recipientValue != null) {
            try {
                recipientId = recipientValue.parseAccountId()
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

        val attachment = Attachment.MessagingAliasSell(dp, alias.aliasName, pricePlanck, dp.blockchainService.height)
        return createTransaction(request, owner, recipientId, 0, attachment)
    }
}
