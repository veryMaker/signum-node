package brs.http;

import brs.assetexchange.AssetExchange;
import brs.util.Convert;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.QUANTITY_MININUM_QNT_PARAMETER;
import static brs.http.common.Parameters.FIRST_INDEX_PARAMETER;
import static brs.http.common.Parameters.LAST_INDEX_PARAMETER;
import static brs.http.common.ResultFields.ASSETS_RESPONSE;

public final class GetAllAssets extends AbstractAssetsRetrieval {

  private final AssetExchange assetExchange;

  public GetAllAssets(AssetExchange assetExchange) {
    super(new APITag[] {APITag.AE}, assetExchange, QUANTITY_MININUM_QNT_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.assetExchange = assetExchange;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {
    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);
    long minimumQuantity = Convert.parseUnsignedLong(req.getParameter(QUANTITY_MININUM_QNT_PARAMETER));

    JsonObject response = new JsonObject();

    response.add(ASSETS_RESPONSE, assetsToJson(assetExchange.getAllAssets(firstIndex, lastIndex).iterator(), minimumQuantity));

    return response;
  }

}
