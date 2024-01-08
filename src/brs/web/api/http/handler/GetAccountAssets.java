package brs.web.api.http.handler;

import brs.Account;
import brs.Account.AccountAsset;
import brs.BurstException;
import brs.services.AccountService;
import brs.services.ParameterService;
import brs.util.CollectionWithIndex;
import brs.util.Convert;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.*;

public final class GetAccountAssets extends ApiServlet.JsonRequestHandler {

    private final ParameterService parameterService;
    private final AccountService accountService;

    public GetAccountAssets(ParameterService parameterService, AccountService accountService) {
        super(new LegacyDocTag[] {LegacyDocTag.ACCOUNTS}, ACCOUNT_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
        this.parameterService = parameterService;
        this.accountService = accountService;
    }

    @Override
    protected
    JsonElement processRequest(HttpServletRequest req) throws BurstException {

        Account account = parameterService.getAccount(req);

        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JsonObject response = new JsonObject();

        JsonArray assetBalances = new JsonArray();
        JsonArray unconfirmedAssetBalances = new JsonArray();

        CollectionWithIndex<AccountAsset> assets = accountService.getAssets(account.getId(), firstIndex, lastIndex);
        for (Account.AccountAsset accountAsset : assets) {
            JsonObject assetBalance = new JsonObject();
            assetBalance.addProperty(ASSET_RESPONSE, Convert.toUnsignedLong(accountAsset.getAssetId()));
            assetBalance.addProperty(BALANCE_QNT_RESPONSE, String.valueOf(accountAsset.getQuantityQNT()));
            assetBalances.add(assetBalance);
            JsonObject unconfirmedAssetBalance = new JsonObject();
            unconfirmedAssetBalance.addProperty(ASSET_RESPONSE, Convert.toUnsignedLong(accountAsset.getAssetId()));
            unconfirmedAssetBalance.addProperty(UNCONFIRMED_BALANCE_QNT_RESPONSE, String.valueOf(accountAsset.getUnconfirmedQuantityQNT()));
            unconfirmedAssetBalances.add(unconfirmedAssetBalance);
        }

        response.add(ASSET_BALANCES_RESPONSE, assetBalances);
        response.add(UNCONFIRMED_ASSET_BALANCES_RESPONSE, unconfirmedAssetBalances);

        if(assets.hasNextIndex()) {
          response.addProperty(NEXT_INDEX_RESPONSE, assets.nextIndex());
        }

        return response;
    }
}
