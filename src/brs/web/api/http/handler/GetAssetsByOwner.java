package brs.web.api.http.handler;

import brs.Account;
import brs.Asset;
import brs.Signum;
import brs.BurstException;
import brs.assetexchange.AssetExchange;
import brs.services.AccountService;
import brs.services.ParameterService;
import brs.util.CollectionWithIndex;
import brs.util.Convert;

import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.ASSETS_RESPONSE;
import static brs.web.api.http.common.ResultFields.NEXT_INDEX_RESPONSE;

public final class GetAssetsByOwner extends AbstractAssetsRetrieval {

  private final ParameterService parameterService;
  private final AssetExchange assetExchange;

  public GetAssetsByOwner(ParameterService parameterService, AssetExchange assetExchange, AccountService accountService) {
    super(new LegacyDocTag[] {LegacyDocTag.AE, LegacyDocTag.ACCOUNTS}, assetExchange, accountService, ACCOUNT_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER,
        HEIGHT_START_PARAMETER, HEIGHT_END_PARAMETER, SKIP_ZERO_VOLUME_PARAMETER);
    this.parameterService = parameterService;
    this.assetExchange = assetExchange;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    Account account = parameterService.getAccount(req);
    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    int heightEnd = Signum.getBlockchain().getHeight();
    // default is one day window
    int heightStart = heightEnd - 360;

    String heightStartString = Convert.emptyToNull(req.getParameter(HEIGHT_START_PARAMETER));
    if(heightStartString != null) {
      heightStart = Integer.parseInt(heightStartString);
    }

    String heightEndString = Convert.emptyToNull(req.getParameter(HEIGHT_END_PARAMETER));
    if(heightEndString != null) {
      heightEnd = Integer.parseInt(heightEndString);
    }

    boolean skipZeroVolume = "true".equalsIgnoreCase(req.getParameter(SKIP_ZERO_VOLUME_PARAMETER));

    JsonObject response = new JsonObject();
    CollectionWithIndex<Asset> assets = assetExchange.getAssetsOwnedBy(account.getId(), firstIndex, lastIndex);
    response.add(ASSETS_RESPONSE, assetsToJson(assets.iterator(),
        heightStart, heightEnd, skipZeroVolume));

    if(assets.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, assets.nextIndex());
    }

    return response;
  }

}
