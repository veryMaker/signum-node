package brs.http

import brs.Attachment
import brs.Constants
import brs.DependencyProvider
import brs.http.JSONResponses.INCORRECT_DGS_LISTING_DESCRIPTION
import brs.http.JSONResponses.INCORRECT_DGS_LISTING_NAME
import brs.http.JSONResponses.INCORRECT_DGS_LISTING_TAGS
import brs.http.JSONResponses.MISSING_NAME
import brs.http.common.Parameters.DESCRIPTION_PARAMETER
import brs.http.common.Parameters.NAME_PARAMETER
import brs.http.common.Parameters.PRICE_NQT_PARAMETER
import brs.http.common.Parameters.QUANTITY_PARAMETER
import brs.http.common.Parameters.TAGS_PARAMETER
import brs.util.convert.emptyToNull
import brs.util.convert.nullToEmpty
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class DGSListing internal constructor(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), NAME_PARAMETER, DESCRIPTION_PARAMETER, TAGS_PARAMETER, QUANTITY_PARAMETER, PRICE_NQT_PARAMETER) {
    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        var name = request.getParameter(NAME_PARAMETER).emptyToNull()
        val description = request.getParameter(DESCRIPTION_PARAMETER).nullToEmpty()
        val tags = request.getParameter(TAGS_PARAMETER).nullToEmpty()
        val priceNQT = ParameterParser.getPriceNQT(request)
        val quantity = ParameterParser.getGoodsQuantity(request)

        if (name == null) {
            return MISSING_NAME
        }
        name = name.trim { it <= ' ' }
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
        val attachment = Attachment.DigitalGoodsListing(dp, name, description, tags, quantity, priceNQT, dp.blockchain.height)
        return createTransaction(request, account, attachment)
    }
}
