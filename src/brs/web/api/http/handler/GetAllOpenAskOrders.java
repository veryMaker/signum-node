package brs.web.api.http.handler;

import brs.Asset;
import brs.Order;
import brs.Order.Ask;
import brs.assetexchange.AssetExchange;
import brs.util.CollectionWithIndex;

import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.FIRST_INDEX_PARAMETER;
import static brs.web.api.http.common.Parameters.LAST_INDEX_PARAMETER;
import static brs.web.api.http.common.ResultFields.NEXT_INDEX_RESPONSE;
import static brs.web.api.http.common.ResultFields.OPEN_ORDERS_RESPONSE;

public final class GetAllOpenAskOrders extends ApiServlet.JsonRequestHandler {

  private final AssetExchange assetExchange;

  public GetAllOpenAskOrders(AssetExchange assetExchange) {
    super(new LegacyDocTag[] {LegacyDocTag.AE}, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.assetExchange = assetExchange;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {

    JsonObject response = new JsonObject();
    JsonArray ordersData = new JsonArray();

    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    Asset asset = null;
    CollectionWithIndex<Ask> orders = assetExchange.getAllAskOrders(firstIndex, lastIndex);
    for (Order.Ask askOrder : orders) {
      if(asset == null || asset.getId() != askOrder.getAssetId()) {
        asset = assetExchange.getAsset(askOrder.getAssetId());
      }
      ordersData.add(JSONData.askOrder(askOrder, asset));
    }

    response.add(OPEN_ORDERS_RESPONSE, ordersData);

    if(orders.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, orders.nextIndex());
    }

    return response;
  }

}
