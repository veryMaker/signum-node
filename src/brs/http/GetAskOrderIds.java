package brs.http;

import brs.BurstException;
import brs.Order;
import brs.Order.Ask;
import brs.assetexchange.AssetExchange;
import brs.services.ParameterService;
import brs.util.CollectionWithIndex;
import brs.util.Convert;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.*;
import static brs.http.common.ResultFields.ASK_ORDER_IDS_RESPONSE;
import static brs.http.common.ResultFields.NEXT_INDEX_RESPONSE;

public final class GetAskOrderIds extends APIServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final AssetExchange assetExchange;

  GetAskOrderIds(ParameterService parameterService, AssetExchange assetExchange) {
    super(new APITag[]{APITag.AE}, ASSET_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.parameterService = parameterService;
    this.assetExchange = assetExchange;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {

    long assetId = parameterService.getAsset(req).getId();
    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    JsonArray orderIds = new JsonArray();
    CollectionWithIndex<Ask> orders = assetExchange.getSortedAskOrders(assetId, firstIndex, lastIndex);
    for (Order.Ask askOrder : orders) {
      orderIds.add(Convert.toUnsignedLong(askOrder.getId()));
    }

    JsonObject response = new JsonObject();
    response.add(ASK_ORDER_IDS_RESPONSE, orderIds);
    
    if(orders.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, orders.nextIndex());
    }

    return response;
  }
}
