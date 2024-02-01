package brs.web.api.http.handler;

import brs.Alias;
import brs.Alias.Offer;
import brs.SignumException;
import brs.services.AliasService;
import brs.util.CollectionWithIndex;
import brs.util.Convert;
import brs.util.TextUtils;

import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.JSONResponses;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.ALIASES_RESPONSE;
import static brs.web.api.http.common.ResultFields.NEXT_INDEX_RESPONSE;

public final class GetAliasesByName extends ApiServlet.JsonRequestHandler {

  private final AliasService aliasService;

  public GetAliasesByName(AliasService aliasService) {
    super(new LegacyDocTag[]{LegacyDocTag.ALIASES}, TIMESTAMP_PARAMETER, ALIAS_NAME_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.aliasService = aliasService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
    final int timestamp = ParameterParser.getTimestamp(req);

    String aliasName = Convert.emptyToNull(req.getParameter(ALIAS_NAME_PARAMETER));
    if(aliasName != null) {
      aliasName = aliasName.trim();
    }
    if (aliasName == null || aliasName.length() < 1 || !TextUtils.isInAlphabetOrUnderline(aliasName)) {
      return JSONResponses.incorrect(ALIAS_NAME_PARAMETER);
    }
    aliasName = "%" + aliasName.toLowerCase() + "%";

    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    JsonArray aliases = new JsonArray();
    CollectionWithIndex<Alias> aliasesByOwner = aliasService.getAliasesByOwner(0L, aliasName, null, firstIndex, lastIndex);
    for (Alias alias : aliasesByOwner) {
      if(alias.getTimestamp() < timestamp) {
        continue;
      }
      final Offer offer = aliasService.getOffer(alias);
      final Alias tld = aliasService.getTLD(alias.getTLD());
      aliases.add(JSONData.alias(alias, tld, offer, 0));
    }

    JsonObject response = new JsonObject();
    response.add(ALIASES_RESPONSE, aliases);

    if(aliasesByOwner.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, aliasesByOwner.nextIndex());
    }

    return response;
  }

}
