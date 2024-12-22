package brs.web.api.http.handler;

import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

public final class GetMyInfo extends ApiServlet.JsonRequestHandler {

  public static final GetMyInfo instance = new GetMyInfo();

  private final String uuid;

  private GetMyInfo() {
    super(new LegacyDocTag[] {LegacyDocTag.INFO});

    uuid = UUID.randomUUID().toString();
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {

    JsonObject response = new JsonObject();
    response.addProperty("host", req.getRemoteHost());
    response.addProperty("address", req.getRemoteAddr());
    response.addProperty("UUID", uuid);
    return response;
  }

}
