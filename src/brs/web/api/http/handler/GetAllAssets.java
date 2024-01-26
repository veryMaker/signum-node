package brs.web.api.http.handler;

import brs.Asset;
import brs.Burst;
import brs.assetexchange.AssetExchange;
import brs.services.AccountService;
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

public final class GetAllAssets extends AbstractAssetsRetrieval {

  private final AssetExchange assetExchange;

  public GetAllAssets(AssetExchange assetExchange, AccountService accountService) {
    super(new LegacyDocTag[] {LegacyDocTag.AE}, assetExchange, accountService, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, HEIGHT_START_PARAMETER, HEIGHT_END_PARAMETER, SKIP_ZERO_VOLUME_PARAMETER);
    this.assetExchange = assetExchange;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {
    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    int heightEnd = Burst.getBlockchain().getHeight();
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

    CollectionWithIndex<Asset> assets = assetExchange.getAllAssets(firstIndex, lastIndex);
    response.add(ASSETS_RESPONSE, assetsToJson(assets.iterator(), heightStart, heightEnd, skipZeroVolume));

    if(assets.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, assets.nextIndex());
    }

    return response;
  }

}
