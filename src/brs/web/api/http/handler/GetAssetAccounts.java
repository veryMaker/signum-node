package brs.web.api.http.handler;

import brs.Account;
import brs.Account.AccountAsset;
import brs.Asset;
import brs.Signum;
import brs.SignumException;
import brs.assetexchange.AssetExchange;
import brs.fluxcapacitor.FluxValues;
import brs.services.ParameterService;
import brs.util.CollectionWithIndex;
import brs.util.Convert;

import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.NEXT_INDEX_RESPONSE;

public final class GetAssetAccounts extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final AssetExchange assetExchange;

  public GetAssetAccounts(ParameterService parameterService, AssetExchange assetExchange) {
    super(new LegacyDocTag[]{LegacyDocTag.AE}, ASSET_PARAMETER, ASSET_IGNORE_TREASURY_PARAMETER, QUANTITY_MININUM_QNT_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.parameterService = parameterService;
    this.assetExchange = assetExchange;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {

    Asset asset = parameterService.getAsset(req);
    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);
    long minimumQuantity = Convert.parseUnsignedLong(req.getParameter(QUANTITY_MININUM_QNT_PARAMETER));
    // default is to filter out ignored accounts
    boolean filterTreasury = "false".equals(req.getParameter(ASSET_IGNORE_TREASURY_PARAMETER)) ? false : true;

    JsonArray accountAssetsArray = new JsonArray();
    boolean unconfirmed = !Signum.getFluxCapacitor().getValue(FluxValues.DISTRIBUTION_FIX);
    CollectionWithIndex<AccountAsset> accountAssets = assetExchange.getAssetAccounts(asset,
        filterTreasury, minimumQuantity, unconfirmed, firstIndex, lastIndex);
    for (Account.AccountAsset accountAsset : accountAssets) {
      accountAssetsArray.add(JSONData.accountAsset(accountAsset));
    }

    JsonObject response = new JsonObject();
    response.add("accountAssets", accountAssetsArray);

    if(accountAssets.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, accountAssets.nextIndex());
    }

    return response;
  }
}
