package brs.web.api.http.handler;

import brs.Asset;
import brs.SignumException;
import brs.Trade;
import brs.assetexchange.AssetExchange;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import brs.web.api.http.common.Parameters;
import brs.util.FilteringIterator;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.TRADES_RESPONSE;

public final class GetAllTrades extends ApiServlet.JsonRequestHandler {

  private final AssetExchange assetExchange;

  public GetAllTrades(AssetExchange assetExchange) {
    super(new LegacyDocTag[] {LegacyDocTag.AE}, TIMESTAMP_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, INCLUDE_ASSET_INFO_PARAMETER);
    this.assetExchange = assetExchange;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
    final int timestamp = ParameterParser.getTimestamp(req);
    final int firstIndex = ParameterParser.getFirstIndex(req);
    final int lastIndex = ParameterParser.getLastIndex(req);
    final boolean includeAssetInfo = !Parameters.isFalse(req.getParameter(INCLUDE_ASSET_INFO_PARAMETER));

    final JsonObject response = new JsonObject();
    final JsonArray trades = new JsonArray();

    FilteringIterator<Trade> tradeIterator = new FilteringIterator<>(
            assetExchange.getAllTrades(0, -1).getCollection(),
            trade -> trade.getTimestamp() >= timestamp, firstIndex, lastIndex);
    Asset asset = null;
    while (tradeIterator.hasNext()) {
      final Trade trade = tradeIterator.next();
      if(includeAssetInfo && (asset == null || asset.getId() != trade.getAssetId())){
        asset = assetExchange.getAsset(trade.getAssetId());
      }

      trades.add(JSONData.trade(trade, asset));
    }

    response.add(TRADES_RESPONSE, trades);
    return response;
  }

}
