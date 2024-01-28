package brs.web.api.http.handler;

import brs.Account;
import brs.SignumException;
import brs.services.ParameterService;
import brs.util.Convert;
import brs.util.JSON;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.ACCOUNT_PARAMETER;
import static brs.web.api.http.common.ResultFields.PUBLIC_KEY_RESPONSE;

public final class GetAccountPublicKey extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;

  public GetAccountPublicKey(ParameterService parameterService) {
    super(new LegacyDocTag[] {LegacyDocTag.ACCOUNTS}, ACCOUNT_PARAMETER);
    this.parameterService = parameterService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {

    Account account = parameterService.getAccount(req);

    if (account.getPublicKey() != null) {
      JsonObject response = new JsonObject();
      response.addProperty(PUBLIC_KEY_RESPONSE, Convert.toHexString(account.getPublicKey()));
      return response;
    } else {
      return JSON.emptyJSON;
    }
  }

}
