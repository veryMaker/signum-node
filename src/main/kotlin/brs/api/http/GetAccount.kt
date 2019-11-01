package brs.api.http

import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.ResultFields.ACCOUNT_RESPONSE
import brs.api.http.common.ResultFields.ASSET_BALANCES_RESPONSE
import brs.api.http.common.ResultFields.ASSET_RESPONSE
import brs.api.http.common.ResultFields.BALANCE_QUANTITY_RESPONSE
import brs.api.http.common.ResultFields.DESCRIPTION_RESPONSE
import brs.api.http.common.ResultFields.NAME_RESPONSE
import brs.api.http.common.ResultFields.PUBLIC_KEY_RESPONSE
import brs.api.http.common.ResultFields.UNCONFIRMED_ASSET_BALANCES_RESPONSE
import brs.api.http.common.ResultFields.UNCONFIRMED_BALANCE_QUANTITY_RESPONSE
import brs.services.AccountService
import brs.services.ParameterService
import brs.util.convert.toHexString
import brs.util.convert.toUnsignedString
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetAccount internal constructor(
    private val parameterService: ParameterService,
    private val accountService: AccountService
) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), ACCOUNT_PARAMETER) {

    override fun processRequest(request: HttpServletRequest): JsonElement {

        val account = parameterService.getAccount(request) ?: return JSONResponses.INCORRECT_ACCOUNT

        val response = JSONData.accountBalance(account)
        JSONData.putAccount(response, ACCOUNT_RESPONSE, account.id)

        if (account.publicKey != null) {
            response.addProperty(PUBLIC_KEY_RESPONSE, account.publicKey!!.toHexString())
        }
        if (!account.name.isNullOrEmpty()) {
            response.addProperty(NAME_RESPONSE, account.name)
        }
        if (!account.description.isNullOrEmpty()) {
            response.addProperty(DESCRIPTION_RESPONSE, account.description)
        }

        val assetBalances = JsonArray()
        val unconfirmedAssetBalances = JsonArray()

        for (accountAsset in accountService.getAssets(account.id, 0, -1)) {
            val assetBalance = JsonObject()
            assetBalance.addProperty(ASSET_RESPONSE, accountAsset.assetId.toUnsignedString())
            assetBalance.addProperty(BALANCE_QUANTITY_RESPONSE, accountAsset.quantity.toString())
            assetBalances.add(assetBalance)
            val unconfirmedAssetBalance = JsonObject()
            unconfirmedAssetBalance.addProperty(ASSET_RESPONSE, accountAsset.assetId.toUnsignedString())
            unconfirmedAssetBalance.addProperty(
                UNCONFIRMED_BALANCE_QUANTITY_RESPONSE,
                accountAsset.unconfirmedQuantity.toString()
            )
            unconfirmedAssetBalances.add(unconfirmedAssetBalance)
        }

        if (assetBalances.size() > 0) {
            response.add(ASSET_BALANCES_RESPONSE, assetBalances)
        }
        if (unconfirmedAssetBalances.size() > 0) {
            response.add(UNCONFIRMED_ASSET_BALANCES_RESPONSE, unconfirmedAssetBalances)
        }

        return response
    }
}
