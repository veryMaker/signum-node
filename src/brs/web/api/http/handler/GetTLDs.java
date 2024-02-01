package brs.web.api.http.handler;

import brs.Alias;
import brs.Alias.Offer;
import brs.SignumException;
import brs.services.AliasService;
import brs.util.CollectionWithIndex;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.*;

public final class GetTLDs extends ApiServlet.JsonRequestHandler {

  private final AliasService aliasService;

  public GetTLDs(AliasService aliasService) {
    super(new LegacyDocTag[]{LegacyDocTag.ALIASES}, TIMESTAMP_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.aliasService = aliasService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
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
      int numberOfAliases = aliasService.getAliasCount(tld.getId());
      tlds.add(JSONData.alias(tld, null, offer, numberOfAliases));
    }

    JsonObject response = new JsonObject();
    response.add(TLDS_RESPONSE, tlds);

    if(tldsList.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, tldsList.nextIndex());
    }

    return response;
  }

}
