package brs.http;

import brs.BurstException;
import brs.services.ParameterService;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.AT_PARAMETER;
import static brs.http.common.Parameters.INCLUDE_DETAILS_PARAMETER;

final class GetAT extends APIServlet.JsonRequestHandler {

  private final ParameterService parameterService;

  GetAT(ParameterService parameterService) {
    super(new APITag[]{APITag.AT}, AT_PARAMETER, INCLUDE_DETAILS_PARAMETER);
    this.parameterService = parameterService;
  }

  @Override
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    boolean includeDetails = !("false".equalsIgnoreCase(req.getParameter(INCLUDE_DETAILS_PARAMETER)));
    return JSONData.at(parameterService.getAT(req), includeDetails);
  }

}
