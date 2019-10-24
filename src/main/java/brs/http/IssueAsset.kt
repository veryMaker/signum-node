package brs.http

import brs.Attachment
import brs.Constants
import brs.DependencyProvider
import brs.http.JSONResponses.INCORRECT_ASSET_DESCRIPTION
import brs.http.JSONResponses.INCORRECT_ASSET_NAME
import brs.http.JSONResponses.INCORRECT_ASSET_NAME_LENGTH
import brs.http.JSONResponses.INCORRECT_DECIMALS
import brs.http.JSONResponses.MISSING_NAME
import brs.http.common.Parameters.DECIMALS_PARAMETER
import brs.http.common.Parameters.DESCRIPTION_PARAMETER
import brs.http.common.Parameters.NAME_PARAMETER
import brs.http.common.Parameters.QUANTITY_QNT_PARAMETER
import brs.util.TextUtils
import brs.util.convert.emptyToNull
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class IssueAsset internal constructor(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.AE, APITag.CREATE_TRANSACTION), NAME_PARAMETER, DESCRIPTION_PARAMETER, QUANTITY_QNT_PARAMETER, DECIMALS_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {

        var name: String? = request.getParameter(NAME_PARAMETER)
        val description = request.getParameter(DESCRIPTION_PARAMETER)
        val decimalsValue = request.getParameter(DECIMALS_PARAMETER).emptyToNull()

        if (name == null) {
            return MISSING_NAME
        }

        name = name.trim { it <= ' ' }
        if (name.length < Constants.MIN_ASSET_NAME_LENGTH || name.length > Constants.MAX_ASSET_NAME_LENGTH) {
            return INCORRECT_ASSET_NAME_LENGTH
        }

        if (!TextUtils.isInAlphabet(name)) {
            return INCORRECT_ASSET_NAME
        }

        if (description != null && description.length > Constants.MAX_ASSET_DESCRIPTION_LENGTH) {
            return INCORRECT_ASSET_DESCRIPTION
        }

        var decimals: Byte = 0
        if (decimalsValue != null) {
            try {
                decimals = java.lang.Byte.parseByte(decimalsValue)
                if (decimals < 0 || decimals > 8) {
                    return INCORRECT_DECIMALS
                }
            } catch (e: NumberFormatException) {
                return INCORRECT_DECIMALS
            }

        }

        val quantityQNT = ParameterParser.getQuantityQNT(request)
        val account = dp.parameterService.getSenderAccount(request)
        val attachment = Attachment.ColoredCoinsAssetIssuance(dp, name, description!!, quantityQNT, decimals, dp.blockchain.height)
        return createTransaction(request, account, attachment)
    }
}
