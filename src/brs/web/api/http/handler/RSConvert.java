package brs.web.api.http.handler;

import brs.util.Convert;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.INCORRECT_ACCOUNT;
import static brs.web.api.http.common.JSONResponses.MISSING_ACCOUNT;
import static brs.web.api.http.common.Parameters.ACCOUNT_PARAMETER;

public final class RSConvert extends ApiServlet.JsonRequestHandler {

  public static final RSConvert instance = new RSConvert();

  private RSConvert() {
    super(new LegacyDocTag[] {LegacyDocTag.ACCOUNTS, LegacyDocTag.UTILS}, ACCOUNT_PARAMETER);
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {
    String accountValue = Convert.emptyToNull(req.getParameter(ACCOUNT_PARAMETER));
    if (accountValue == null) {
      return MISSING_ACCOUNT;
    }
    try {
      long accountId = Convert.parseAccountId(accountValue);
      if (accountId == 0) {
        return INCORRECT_ACCOUNT;
      }
      JsonObject response = new JsonObject();
      JSONData.putAccount(response, "account", accountId);
      return response;
    } catch (RuntimeException e) {
      return INCORRECT_ACCOUNT;
    }
  }

}
