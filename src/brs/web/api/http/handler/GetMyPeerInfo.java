package brs.web.api.http.handler;

import brs.TransactionProcessor;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

public final class GetMyPeerInfo extends ApiServlet.JsonRequestHandler {

  private final TransactionProcessor transactionProcessor;

  public GetMyPeerInfo(TransactionProcessor transactionProcessor) {
    super(new LegacyDocTag[]{LegacyDocTag.PEER_INFO});
    this.transactionProcessor = transactionProcessor;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {

    JsonObject response = new JsonObject();
    response.addProperty("utsInStore", transactionProcessor.getAmountUnconfirmedTransactions());
    return response;
  }

}
