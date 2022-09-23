package brs.http;

import brs.Account;
import brs.Asset;
import brs.BurstException;
import brs.Order;
import brs.Trade;
import brs.TransactionType;
import brs.Order.OrderJournal;
import brs.assetexchange.AssetExchange;
import brs.services.ParameterService;
import brs.util.Convert;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static brs.http.common.Parameters.*;
import static brs.http.common.ResultFields.*;

public final class GetTradeJournal extends APIServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final AssetExchange assetExchange;

  GetTradeJournal(ParameterService parameterService, AssetExchange assetExchange) {
    super(new APITag[] {APITag.AE}, ASSET_PARAMETER, ACCOUNT_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.parameterService = parameterService;
    this.assetExchange = assetExchange;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    
    final Asset asset = parameterService.getAsset(req);
    final Account account = parameterService.getAccount(req);

    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    JsonObject response = new JsonObject();
    JsonArray journalData = new JsonArray();
    
    JSONData.putAccount(response, ACCOUNT_RESPONSE, account.getId());
    response.addProperty(ASSET_RESPONSE, Convert.toUnsignedLong(asset.getId()));

    Collection<OrderJournal> orders = assetExchange.getOrderJournal(account.getId(), asset.getId(), firstIndex, lastIndex);
    for (OrderJournal order : orders) {
      JsonObject orderData = JSONData.order(order, asset);
      
      orderData.addProperty(EXECUTED_QUANTITY_QNT_RESPONSE, String.valueOf(order.getExecutedAmountQNT()));
      orderData.addProperty(EXECUTED_VOLUME_QNT_RESPONSE, String.valueOf(order.getExecutedVolumeNQT()));
      orderData.addProperty(TYPE_RESPONSE, order.getSubtype() == TransactionType.SUBTYPE_COLORED_COINS_ASK_ORDER_PLACEMENT ? "ask" : "bid");
      orderData.addProperty(STATUS_RESPONSE, order.getStatus() == Order.ORDER_STATUS_OPEN ? "open" :
        order.getStatus() == Order.ORDER_STATUS_FILLED ? "filled" : "closed");
      
      JsonArray tradesData = new JsonArray();
      for(Trade trade : order.getTrades()) {
        tradesData.add(JSONData.trade(trade, asset));
      }
      orderData.add(TRADES_RESPONSE, tradesData);
      
      journalData.add(orderData);
    }
    response.add(TRADE_JOURNAL_RESPONSE, journalData);

    return response;
  }
}
