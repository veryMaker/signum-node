package brs.http;

import brs.Burst;
import brs.at.AT.AtMapEntry;
import brs.util.Convert;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.ResultFields.VALUE_RESPONSE;
import static brs.http.common.ResultFields.KEY_VALUES_RESPONSE;

import java.util.Collection;

import static brs.http.common.Parameters.AT_PARAMETER;
import static brs.http.common.Parameters.KEY1_PARAMETER;
import static brs.http.common.Parameters.KEY2_PARAMETER;


final class GetATMapValues extends APIServlet.JsonRequestHandler {


  GetATMapValues() {
    super(new APITag[] {APITag.AT}, AT_PARAMETER, KEY1_PARAMETER);
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {

    long atId = Convert.parseUnsignedLong(req.getParameter(AT_PARAMETER));
    long key1 = Convert.parseUnsignedLong(req.getParameter(KEY1_PARAMETER));

    Collection<AtMapEntry> list = Burst.getStores().getAtStore().getMapValues(atId, key1);

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
