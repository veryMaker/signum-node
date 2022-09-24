package brs.http;

import brs.Asset;
import brs.Order;
import brs.Order.Bid;
import brs.assetexchange.AssetExchange;
import brs.util.CollectionWithIndex;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.FIRST_INDEX_PARAMETER;
import static brs.http.common.Parameters.LAST_INDEX_PARAMETER;
import static brs.http.common.ResultFields.NEXT_INDEX_RESPONSE;

public final class GetAllOpenBidOrders extends APIServlet.JsonRequestHandler {

  private final AssetExchange assetExchange;

  GetAllOpenBidOrders(AssetExchange assetExchange) {
    super(new APITag[] {APITag.AE}, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
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
    CollectionWithIndex<Bid> orders = assetExchange.getAllBidOrders(firstIndex, lastIndex);
    for (Order.Bid bidOrder : orders) {
      if(asset == null || asset.getId() != bidOrder.getAssetId()) {
        asset = assetExchange.getAsset(bidOrder.getAssetId());
      }
      ordersData.add(JSONData.bidOrder(bidOrder, asset));
    }

    response.add("openOrders", ordersData);
    
    if(orders.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, orders.nextIndex());
    }
    
    return response;
  }

}
