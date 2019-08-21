package brs.http

import brs.Account
import brs.BurstException
import brs.services.AccountService
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.ACCOUNT_PARAMETER
import brs.http.common.ResultFields.*

internal class GetAccount internal constructor(private val parameterService: ParameterService, private val accountService: AccountService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), ACCOUNT_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val account = parameterService.getAccount(req)

        val response = JSONData.accountBalance(account)
        JSONData.putAccount(response, ACCOUNT_RESPONSE, account.getId())

        if (account.publicKey != null) {
            response.addProperty(PUBLIC_KEY_RESPONSE, Convert.toHexString(account.publicKey))
        }
        if (account.name != null) {
            response.addProperty(NAME_RESPONSE, account.name)
        }
        if (account.description != null) {
            response.addProperty(DESCRIPTION_RESPONSE, account.description)
        }

        val assetBalances = JsonArray()
        val unconfirmedAssetBalances = JsonArray()

        for (accountAsset in accountService.getAssets(account.getId(), 0, -1)) {
            val assetBalance = JsonObject()
            assetBalance.addProperty(ASSET_RESPONSE, Convert.toUnsignedLong(accountAsset.getAssetId()))
            assetBalance.addProperty(BALANCE_QNT_RESPONSE, accountAsset.quantityQNT.toString())
            assetBalances.add(assetBalance)
            val unconfirmedAssetBalance = JsonObject()
            unconfirmedAssetBalance.addProperty(ASSET_RESPONSE, Convert.toUnsignedLong(accountAsset.getAssetId()))
            unconfirmedAssetBalance.addProperty(UNCONFIRMED_BALANCE_QNT_RESPONSE, accountAsset.unconfirmedQuantityQNT.toString())
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
