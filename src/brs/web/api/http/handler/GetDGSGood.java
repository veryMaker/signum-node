package brs.web.api.http.handler;

import brs.BurstException;
import brs.services.ParameterService;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.GOODS_PARAMETER;

public final class GetDGSGood extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;

  public GetDGSGood(ParameterService parameterService) {
    super(new LegacyDocTag[] {LegacyDocTag.DGS}, GOODS_PARAMETER);
    this.parameterService = parameterService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    return JSONData.goods(parameterService.getGoods(req));
  }

}
