package brs.web.api.http.handler;

import brs.SignumException;
import brs.Order;
import brs.Order.Bid;
import brs.assetexchange.AssetExchange;
import brs.services.ParameterService;
import brs.util.CollectionWithIndex;
import brs.util.Convert;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.NEXT_INDEX_RESPONSE;

public final class GetBidOrderIds extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final AssetExchange assetExchange;

  public GetBidOrderIds(ParameterService parameterService, AssetExchange assetExchange) {
    super(new LegacyDocTag[]{LegacyDocTag.AE}, ASSET_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.parameterService = parameterService;
    this.assetExchange = assetExchange;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
    long assetId = parameterService.getAsset(req).getId();
    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    JsonArray orderIds = new JsonArray();
    CollectionWithIndex<Bid> orders = assetExchange.getSortedBidOrders(assetId, firstIndex, lastIndex);
    for (Order.Bid bidOrder : orders) {
      orderIds.add(Convert.toUnsignedLong(bidOrder.getId()));
    }
    JsonObject response = new JsonObject();
    response.add("bidOrderIds", orderIds);

    if(orders.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, orders.nextIndex());
    }

    return response;
  }

}
