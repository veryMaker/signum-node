package brs.web.api.http.handler;

import brs.SignumException;
import brs.services.ParameterService;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.AT_PARAMETER;
import static brs.web.api.http.common.Parameters.HEIGHT_PARAMETER;

public class GetATDetails extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;

  public GetATDetails(ParameterService parameterService) {
    super(new LegacyDocTag[] {LegacyDocTag.AT}, AT_PARAMETER, HEIGHT_PARAMETER);
    this.parameterService = parameterService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
    return JSONData.at(parameterService.getAT(req));
  }
}
