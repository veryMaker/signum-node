package brs.http;

import brs.Alias;
import brs.Alias.Offer;
import brs.BurstException;
import brs.services.AliasService;
import brs.services.ParameterService;
import brs.util.CollectionWithIndex;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.*;
import static brs.http.common.ResultFields.ALIASES_RESPONSE;
import static brs.http.common.ResultFields.NEXT_INDEX_RESPONSE;

public final class GetAliases extends APIServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final AliasService aliasService;

  GetAliases(ParameterService parameterService, AliasService aliasService) {
    super(new APITag[]{APITag.ALIASES}, TIMESTAMP_PARAMETER, ACCOUNT_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.parameterService = parameterService;
    this.aliasService = aliasService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    final int timestamp = ParameterParser.getTimestamp(req);
    final long accountId = parameterService.getAccount(req).getId();
    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    JsonArray aliases = new JsonArray();
    CollectionWithIndex<Alias> aliasesByOwner = aliasService.getAliasesByOwner(accountId, firstIndex, lastIndex);
    for (Alias alias : aliasesByOwner) {
      if(alias.getTimestamp() < timestamp) {
        continue;
      }
      final Offer offer = aliasService.getOffer(alias);
      aliases.add(JSONData.alias(alias, offer));
    }

    JsonObject response = new JsonObject();
    response.add(ALIASES_RESPONSE, aliases);
    
    if(aliasesByOwner.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, aliasesByOwner.nextIndex());
    }

    return response;
  }

}
