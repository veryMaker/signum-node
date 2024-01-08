package brs.web.api.http.handler;

import brs.*;
import brs.assetexchange.AssetExchange;
import brs.services.ParameterService;
import brs.web.api.http.common.APITransactionManager;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.UNKNOWN_ORDER;
import static brs.web.api.http.common.Parameters.ORDER_PARAMETER;

public final class CancelBidOrder extends CreateTransaction {

  private final ParameterService parameterService;
  private final Blockchain blockchain;
  private final AssetExchange assetExchange;

  public CancelBidOrder(ParameterService parameterService, Blockchain blockchain, AssetExchange assetExchange, APITransactionManager apiTransactionManager) {
    super(new LegacyDocTag[] {LegacyDocTag.AE, LegacyDocTag.CREATE_TRANSACTION}, apiTransactionManager, ORDER_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
    this.assetExchange = assetExchange;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    long orderId = ParameterParser.getOrderId(req);
    Account account = parameterService.getSenderAccount(req);
    Order.Bid orderData = assetExchange.getBidOrder(orderId);
    if (orderData == null || orderData.getAccountId() != account.getId()) {
      return UNKNOWN_ORDER;
    }
    Attachment attachment = new Attachment.ColoredCoinsBidOrderCancellation(orderId, blockchain.getHeight());
    return createTransaction(req, account, attachment);
  }

}
