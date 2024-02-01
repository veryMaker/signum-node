package brs.web.api.http.handler;

import brs.Asset;
import brs.SignumException;
import brs.Order;
import brs.assetexchange.AssetExchange;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.UNKNOWN_ORDER;
import static brs.web.api.http.common.Parameters.ORDER_PARAMETER;

public final class GetAskOrder extends ApiServlet.JsonRequestHandler {

  private final AssetExchange assetExchange;

  public GetAskOrder(AssetExchange assetExchange) {
    super(new LegacyDocTag[] {LegacyDocTag.AE}, ORDER_PARAMETER);
    this.assetExchange = assetExchange;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
    long orderId = ParameterParser.getOrderId(req);
    Order.Ask askOrder = assetExchange.getAskOrder(orderId);
    if (askOrder == null) {
      return UNKNOWN_ORDER;
    }
    Asset asset = assetExchange.getAsset(askOrder.getAssetId());
    return JSONData.askOrder(askOrder, asset);
  }

}
