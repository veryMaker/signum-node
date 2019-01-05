package brs.http;

import brs.BurstException;
import brs.Order;
import brs.assetexchange.AssetExchange;
import brs.db.BurstIterator;
import brs.services.ParameterService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.*;
import static brs.http.common.ResultFields.ASK_ORDERS_RESPONSE;

public final class GetAskOrders extends APIServlet.APIRequestHandler {

  private final ParameterService parameterService;
  private final AssetExchange assetExchange;

  GetAskOrders(ParameterService parameterService, AssetExchange assetExchange) {
    super(new APITag[] {APITag.AE}, ASSET_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.parameterService = parameterService;
    this.assetExchange = assetExchange;
  }

  @Override
  JSONStreamAware processRequest(HttpServletRequest req) throws BurstException {

    long assetId = parameterService.getAsset(req).getId();
    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    JSONArray orders = new JSONArray();
    try (BurstIterator<Order.Ask> askOrders = assetExchange.getSortedAskOrders(assetId, firstIndex, lastIndex)) {
      while (askOrders.hasNext()) {
        orders.add(JSONData.askOrder(askOrders.next()));
      }
    }

    JSONObject response = new JSONObject();
    response.put(ASK_ORDERS_RESPONSE, orders);
    return response;

  }

}
