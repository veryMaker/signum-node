package brs.web.api.http.handler;

import brs.TransactionProcessor;
import brs.props.PropertyService;
import brs.props.Props;

import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.ERROR_NOT_ALLOWED;
import static brs.web.api.http.common.Parameters.API_KEY_PARAMETER;
import static brs.web.api.http.common.ResultFields.DONE_RESPONSE;
import static brs.web.api.http.common.ResultFields.ERROR_RESPONSE;

import java.util.List;

public final class ClearUnconfirmedTransactions extends ApiServlet.JsonRequestHandler {

  private final TransactionProcessor transactionProcessor;

  private final List<String> apiAdminKeyList;

  public ClearUnconfirmedTransactions(TransactionProcessor transactionProcessor, PropertyService propertyService) {
    super(new LegacyDocTag[]{LegacyDocTag.ADMIN}, API_KEY_PARAMETER);
    this.transactionProcessor = transactionProcessor;

    apiAdminKeyList = propertyService.getStringList(Props.API_ADMIN_KEY_LIST);
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {
    String apiKey = req.getParameter(API_KEY_PARAMETER);
    if(!apiAdminKeyList.contains(apiKey)) {
      return ERROR_NOT_ALLOWED;
    }

    JsonObject response = new JsonObject();
    try {
      transactionProcessor.clearUnconfirmedTransactions();
      response.addProperty(DONE_RESPONSE, true);
    } catch (RuntimeException e) {
      response.addProperty(ERROR_RESPONSE, e.toString());
    }
    return response;
  }

  final boolean requirePost() {
    return true;
  }

}
