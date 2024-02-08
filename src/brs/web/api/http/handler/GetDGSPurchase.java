package brs.web.api.http.handler;

import brs.SignumException;
import brs.services.ParameterService;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.PURCHASE_PARAMETER;

public final class GetDGSPurchase extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;

  public GetDGSPurchase(ParameterService parameterService) {
    super(new LegacyDocTag[] {LegacyDocTag.DGS}, PURCHASE_PARAMETER);
    this.parameterService = parameterService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
    return JSONData.purchase(parameterService.getPurchase(req));
  }

}
