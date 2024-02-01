package brs.web.api.http.handler;

import brs.Asset;
import brs.assetexchange.AssetExchange;
import brs.util.CollectionWithIndex;
import brs.util.Convert;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.FIRST_INDEX_PARAMETER;
import static brs.web.api.http.common.Parameters.LAST_INDEX_PARAMETER;
import static brs.web.api.http.common.ResultFields.ASSET_IDS_RESPONSE;
import static brs.web.api.http.common.ResultFields.NEXT_INDEX_RESPONSE;

public final class GetAssetIds extends ApiServlet.JsonRequestHandler {

  private final AssetExchange assetExchange;

  public GetAssetIds(AssetExchange assetExchange) {
    super(new LegacyDocTag[] {LegacyDocTag.AE}, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.assetExchange = assetExchange;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {

    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    JsonArray assetIds = new JsonArray();
    CollectionWithIndex<Asset> assets = assetExchange.getAllAssets(firstIndex, lastIndex);
    for (Asset asset : assets) {
      assetIds.add(Convert.toUnsignedLong(asset.getId()));
    }
    JsonObject response = new JsonObject();
    response.add(ASSET_IDS_RESPONSE, assetIds);

    if(assets.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, assets.nextIndex());
    }

    return response;
  }

}
