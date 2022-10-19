package brs.http;

import brs.Burst;
import brs.util.Convert;
import brs.util.TextUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.http.JSONResponses.*;
import static brs.http.common.Parameters.*;
import static brs.http.common.ResultFields.VALUE_RESPONSE;


final class GetATMapValue extends APIServlet.JsonRequestHandler {


  GetATMapValue() {
    super(new APITag[] {APITag.AT}, AT_PARAMETER, KEY1_PARAMETER, KEY2_PARAMETER);
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {

    String at = req.getParameter(AT_PARAMETER);
    if (at == null) {
      return MISSING_AT;
    }

    String key1 = req.getParameter(KEY1_PARAMETER);
    if (key1 == null) {
      return MISSING_AT_KEY1;
    }

    String key2 = req.getParameter(KEY2_PARAMETER);
    if (key2 == null) {
      return MISSING_AT_KEY2;
    }

    long atId = Convert.parseUnsignedLong(at);
    long k1 = Convert.parseUnsignedLong(key1);
    long k2 = Convert.parseUnsignedLong(key2);

    String value = Convert.toUnsignedLong(Burst.getStores().getAtStore().getMapValue(atId, k1, k2));

    JsonObject response = new JsonObject();
    response.addProperty(VALUE_RESPONSE, value);
    return response;
  }

}
