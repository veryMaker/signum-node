package brs.web.api.http.handler;

import brs.services.TimeService;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.ResultFields.TIME_RESPONSE;

public final class GetTime extends ApiServlet.JsonRequestHandler {

  private final TimeService timeService;

  public GetTime(TimeService timeService) {
    super(new LegacyDocTag[]{LegacyDocTag.INFO});
    this.timeService = timeService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {
    JsonObject response = new JsonObject();
    response.addProperty(TIME_RESPONSE, timeService.getEpochTime());

    return response;
  }

}
