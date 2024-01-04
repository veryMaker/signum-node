package brs.web.api.http.handler;

import brs.Asset;
import brs.BurstException;
import brs.Order;
import brs.Order.Ask;
import brs.assetexchange.AssetExchange;
import brs.services.ParameterService;
import brs.util.CollectionWithIndex;

import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.ASK_ORDERS_RESPONSE;
import static brs.web.api.http.common.ResultFields.NEXT_INDEX_RESPONSE;

public final class GetAskOrders extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final AssetExchange assetExchange;

  public GetAskOrders(ParameterService parameterService, AssetExchange assetExchange) {
    super(new LegacyDocTag[] {LegacyDocTag.AE}, ASSET_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.parameterService = parameterService;
    this.assetExchange = assetExchange;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {

    long assetId = parameterService.getAsset(req).getId();
    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    JsonArray ordersData = new JsonArray();
    Asset asset = null;
    CollectionWithIndex<Ask> orders = assetExchange.getSortedAskOrders(assetId, firstIndex, lastIndex);
    for (Order.Ask askOrder : orders) {
      if(asset == null || asset.getId() != askOrder.getAssetId()) {
        asset = assetExchange.getAsset(askOrder.getAssetId());
      }
      ordersData.add(JSONData.askOrder(askOrder, asset));
    }

    JsonObject response = new JsonObject();
    response.add(ASK_ORDERS_RESPONSE, ordersData);

    if(orders.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, orders.nextIndex());
    }

    return response;
  }
}
