package brs.http;

import brs.Burst;
import brs.at.AT.AtMapEntry;
import brs.util.Convert;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.http.JSONResponses.*;
import static brs.http.common.ResultFields.VALUE_RESPONSE;
import static brs.http.common.ResultFields.KEY_VALUES_RESPONSE;

import java.util.Collection;

import static brs.http.common.Parameters.AT_PARAMETER;
import static brs.http.common.Parameters.KEY1_PARAMETER;
import static brs.http.common.Parameters.KEY2_PARAMETER;
import static brs.http.common.Parameters.VALUE_PARAMETER;


final class GetATMapValues extends APIServlet.JsonRequestHandler {


  GetATMapValues() {
    super(new APITag[] {APITag.AT}, AT_PARAMETER, KEY1_PARAMETER, VALUE_PARAMETER);
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
    String valueString = Convert.emptyToNull(req.getParameter(VALUE_PARAMETER));
    Long value = null;
    if(valueString != null) {
        value = Convert.parseUnsignedLong(valueString);
    }

    long atId = Convert.parseUnsignedLong(at);
    long k1 = Convert.parseUnsignedLong(key1);

    Collection<AtMapEntry> list = Burst.getStores().getAtStore().getMapValues(atId, k1, value);

    JsonArray mapValues = new JsonArray();
    for (AtMapEntry entry : list) {
      JsonObject entryJson = new JsonObject();
      entryJson.addProperty(KEY2_PARAMETER, Convert.toUnsignedLong(entry.getKey2()));
      entryJson.addProperty(VALUE_RESPONSE, Convert.toUnsignedLong(entry.getValue()));
      mapValues.add(entryJson);
    }

    JsonObject response = new JsonObject();
    response.add(KEY_VALUES_RESPONSE, mapValues);
    return response;
  }

}
