package brs.api.http

import brs.api.http.common.JSONResponses.INCORRECT_DGS_LISTING_DESCRIPTION
import brs.api.http.common.JSONResponses.INCORRECT_DGS_LISTING_NAME
import brs.api.http.common.JSONResponses.INCORRECT_DGS_LISTING_TAGS
import brs.api.http.common.JSONResponses.MISSING_NAME
import brs.api.http.common.Parameters.DESCRIPTION_PARAMETER
import brs.api.http.common.Parameters.NAME_PARAMETER
import brs.api.http.common.Parameters.PRICE_PLANCK_PARAMETER
import brs.api.http.common.Parameters.QUANTITY_PARAMETER
import brs.api.http.common.Parameters.TAGS_PARAMETER
import brs.entity.DependencyProvider
import brs.objects.Constants
import brs.transaction.appendix.Attachment
import brs.util.convert.emptyToNull
import brs.util.jetty.get
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class DGSListing internal constructor(private val dp: DependencyProvider) : CreateTransaction(
    dp,
    arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION),
    NAME_PARAMETER,
    DESCRIPTION_PARAMETER,
    TAGS_PARAMETER,
    QUANTITY_PARAMETER,
    PRICE_PLANCK_PARAMETER
) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        var name = request[NAME_PARAMETER].emptyToNull()
        val description = request[DESCRIPTION_PARAMETER].orEmpty()
        val tags = request[TAGS_PARAMETER].orEmpty()
        val pricePlanck = ParameterParser.getPricePlanck(request)
        val quantity = ParameterParser.getGoodsQuantity(request)

        if (name == null) {
            return MISSING_NAME
        }
        name = name.trim()
        if (name.length > Constants.MAX_DGS_LISTING_NAME_LENGTH) {
            return INCORRECT_DGS_LISTING_NAME
        }

        if (description.length > Constants.MAX_DGS_LISTING_DESCRIPTION_LENGTH) {
            return INCORRECT_DGS_LISTING_DESCRIPTION
        }

        if (tags.length > Constants.MAX_DGS_LISTING_TAGS_LENGTH) {
            return INCORRECT_DGS_LISTING_TAGS
        }

        val account = dp.parameterService.getSenderAccount(request)
        val attachment = Attachment.DigitalGoodsListing(
            dp,
            name,
            description,
            tags,
            quantity,
            pricePlanck,
            dp.blockchainService.height
        )
        return createTransaction(request, account, attachment)
    }
}
