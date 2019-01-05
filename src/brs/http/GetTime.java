package brs.http;

import brs.services.TimeService;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.ResultFields.TIME_RESPONSE;

public final class GetTime extends APIServlet.APIRequestHandler {

  private final TimeService timeService;

  GetTime(TimeService timeService) {
    super(new APITag[]{APITag.INFO});
    this.timeService = timeService;
  }

  @Override
  JSONStreamAware processRequest(HttpServletRequest req) {
    JSONObject response = new JSONObject();
    response.put(TIME_RESPONSE, timeService.getEpochTime());

    return response;
  }

}
