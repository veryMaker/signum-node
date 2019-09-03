package brs.http

import brs.*
import brs.http.JSONResponses.INCORRECT_DGS_LISTING_DESCRIPTION
import brs.http.JSONResponses.INCORRECT_DGS_LISTING_NAME
import brs.http.JSONResponses.INCORRECT_DGS_LISTING_TAGS
import brs.http.JSONResponses.MISSING_NAME
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

internal class DGSListing internal constructor(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), NAME_PARAMETER, DESCRIPTION_PARAMETER, TAGS_PARAMETER, QUANTITY_PARAMETER, PRICE_NQT_PARAMETER) {
    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        var name = Convert.emptyToNull(req.getParameter(NAME_PARAMETER))
        val description = Convert.nullToEmpty(req.getParameter(DESCRIPTION_PARAMETER))
        val tags = Convert.nullToEmpty(req.getParameter(TAGS_PARAMETER))
        val priceNQT = ParameterParser.getPriceNQT(req)
        val quantity = ParameterParser.getGoodsQuantity(req)

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

        val account = dp.parameterService.getSenderAccount(req)
        val attachment = Attachment.DigitalGoodsListing(name, description, tags, quantity, priceNQT, dp.blockchain.height)
        return createTransaction(req, account, attachment)
    }
}
