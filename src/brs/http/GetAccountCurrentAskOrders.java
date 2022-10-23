package brs.http;

import brs.Asset;
import brs.BurstException;
import brs.Order;
import brs.assetexchange.AssetExchange;
import brs.services.ParameterService;
import brs.util.CollectionWithIndex;
import brs.util.Convert;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.*;
import static brs.http.common.ResultFields.ASK_ORDERS_RESPONSE;
import static brs.http.common.ResultFields.NEXT_INDEX_RESPONSE;

public final class GetAccountCurrentAskOrders extends APIServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final AssetExchange assetExchange;

  GetAccountCurrentAskOrders(ParameterService parameterService, AssetExchange assetExchange) {
    super(new APITag[]{APITag.ACCOUNTS, APITag.AE}, ACCOUNT_PARAMETER, ASSET_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.parameterService = parameterService;
    this.assetExchange = assetExchange;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    final long accountId = parameterService.getAccount(req).getId();

    long assetId = 0;
    try {
      assetId = Convert.parseUnsignedLong(req.getParameter(ASSET_PARAMETER));
    } catch (RuntimeException e) {
      // ignore
    }
    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    CollectionWithIndex<Order.Ask> askOrders;
    if (assetId == 0) {
      askOrders = assetExchange.getAskOrdersByAccount(accountId, firstIndex, lastIndex);
    } else {
      askOrders = assetExchange.getAskOrdersByAccountAsset(accountId, assetId, firstIndex, lastIndex);
    }
    JsonArray orders = new JsonArray();
    Asset asset = null;
    for (Order.Ask order : askOrders) {
      if(asset == null || asset.getId() != order.getAssetId()) {
        asset = assetExchange.getAsset(order.getAssetId());
      }
      orders.add(JSONData.askOrder(order, asset));
    }
    JsonObject response = new JsonObject();
    response.add(ASK_ORDERS_RESPONSE, orders);
    
    if(askOrders.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, askOrders.nextIndex());
    }

    return response;
  }

}
