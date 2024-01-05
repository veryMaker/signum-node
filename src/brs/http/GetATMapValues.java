package brs.http;

import brs.Burst;
import brs.at.AT.AtMapEntry;
import brs.util.CollectionWithIndex;
import brs.util.Convert;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.http.JSONResponses.*;
import static brs.http.common.Parameters.*;
import static brs.http.common.ResultFields.*;

import java.util.Collection;


final class GetATMapValues extends APIServlet.JsonRequestHandler {


  GetATMapValues() {
    super(new APITag[] {APITag.AT},
      AT_PARAMETER,
      KEY1_PARAMETER,
      VALUE_PARAMETER,
      FIRST_INDEX_PARAMETER,
      LAST_INDEX_PARAMETER
    );
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


    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex  = ParameterParser.getLastIndex(req);

    if(lastIndex < firstIndex) {
      throw new IllegalArgumentException("lastIndex must be greater or equal to firstIndex");
    }

    CollectionWithIndex<AtMapEntry> atMapEntries = Burst.getStores().getAtStore().getMapValues(atId, k1, value, firstIndex, lastIndex);

    JsonArray mapValues = new JsonArray();
    for (AtMapEntry entry : atMapEntries) {
      JsonObject entryJson = new JsonObject();
      entryJson.addProperty(KEY2_PARAMETER, Convert.toUnsignedLong(entry.getKey2()));
      entryJson.addProperty(VALUE_RESPONSE, Convert.toUnsignedLong(entry.getValue()));
      mapValues.add(entryJson);
    }

    JsonObject response = new JsonObject();
    response.add(KEY_VALUES_RESPONSE, mapValues);
    if(atMapEntries.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, atMapEntries.nextIndex());
    }
    return response;
  }

}
