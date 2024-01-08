package brs.web.api.http.handler;

import brs.BurstException;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.Parameters;
import brs.services.ParameterService;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

public final class GetBalance extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;

  public GetBalance(ParameterService parameterService) {
    super(new LegacyDocTag[]{LegacyDocTag.ACCOUNTS}, Parameters.ACCOUNT_PARAMETER);
    this.parameterService = parameterService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    return JSONData.accountBalance(parameterService.getAccount(req));
  }

}
