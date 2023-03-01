package brs.http;

import brs.Alias;
import brs.Alias.Offer;
import brs.BurstException;
import brs.services.AliasService;
import brs.util.CollectionWithIndex;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.*;
import static brs.http.common.ResultFields.*;

public final class GetTLDs extends APIServlet.JsonRequestHandler {

  private final AliasService aliasService;

  GetTLDs(AliasService aliasService) {
    super(new APITag[]{APITag.ALIASES}, TIMESTAMP_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.aliasService = aliasService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    final int timestamp = ParameterParser.getTimestamp(req);
    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    JsonArray tlds = new JsonArray();
    CollectionWithIndex<Alias> tldsList = aliasService.getTLDs(firstIndex, lastIndex);
    for (Alias tld : tldsList) {
      if(tld.getTimestamp() < timestamp) {
        continue;
      }
      final Offer offer = aliasService.getOffer(tld);
      tlds.add(JSONData.alias(tld, offer));
    }

    JsonObject response = new JsonObject();
    response.add(TLDS_RESPONSE, tlds);
    
    if(tldsList.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, tldsList.nextIndex());
    }

    return response;
  }

}
