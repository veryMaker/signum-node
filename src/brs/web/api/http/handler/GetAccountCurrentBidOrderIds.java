package brs.web.api.http.handler;

import brs.SignumException;
import brs.Order;
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
import static brs.web.api.http.common.ResultFields.BID_ORDER_IDS_RESPONSE;
import static brs.web.api.http.common.ResultFields.NEXT_INDEX_RESPONSE;

public final class GetAccountCurrentBidOrderIds extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final AssetExchange assetExchange;

  public GetAccountCurrentBidOrderIds(ParameterService parameterService, AssetExchange assetExchange) {
    super(new LegacyDocTag[] {LegacyDocTag.ACCOUNTS, LegacyDocTag.AE}, ACCOUNT_PARAMETER, ASSET_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.parameterService = parameterService;
    this.assetExchange = assetExchange;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {

    long accountId = parameterService.getAccount(req).getId();
    long assetId = 0;
    try {
      assetId = Convert.parseUnsignedLong(req.getParameter(ASSET_PARAMETER));
    } catch (RuntimeException e) {
      // ignore
    }
    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    CollectionWithIndex<Order.Bid> bidOrders;
    if (assetId == 0) {
      bidOrders = assetExchange.getBidOrdersByAccount(accountId, firstIndex, lastIndex);
    } else {
      bidOrders = assetExchange.getBidOrdersByAccountAsset(accountId, assetId, firstIndex, lastIndex);
    }
    JsonArray orderIds = new JsonArray();
    for (Order.Bid bidOrder : bidOrders) {
      orderIds.add(Convert.toUnsignedLong(bidOrder.getId()));
    }
    JsonObject response = new JsonObject();
    response.add(BID_ORDER_IDS_RESPONSE, orderIds);

    if(bidOrders.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, bidOrders.nextIndex());
    }

    return response;
  }

}
