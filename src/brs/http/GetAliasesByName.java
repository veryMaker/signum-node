package brs.http;

import brs.Alias;
import brs.Alias.Offer;
import brs.BurstException;
import brs.services.AliasService;
import brs.util.CollectionWithIndex;
import brs.util.Convert;
import brs.util.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.*;
import static brs.http.common.ResultFields.ALIASES_RESPONSE;
import static brs.http.common.ResultFields.NEXT_INDEX_RESPONSE;

public final class GetAliasesByName extends APIServlet.JsonRequestHandler {

  private final AliasService aliasService;

  GetAliasesByName(AliasService aliasService) {
    super(new APITag[]{APITag.ALIASES}, TIMESTAMP_PARAMETER, ALIAS_NAME_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.aliasService = aliasService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
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
