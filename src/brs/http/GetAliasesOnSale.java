package brs.http;

import brs.Alias;
import brs.BurstException;
import brs.services.AliasService;
import brs.util.CollectionWithIndex;
import brs.util.Convert;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.*;
import static brs.http.common.ResultFields.ALIASES_RESPONSE;
import static brs.http.common.ResultFields.NEXT_INDEX_RESPONSE;

public final class GetAliasesOnSale extends APIServlet.JsonRequestHandler {

  private final AliasService aliasService;

  GetAliasesOnSale(AliasService aliasService) {
    super(new APITag[]{APITag.ALIASES}, ACCOUNT_PARAMETER, BUYER_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.aliasService = aliasService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);
    
    long account = 0L;
    String accountId = Convert.emptyToNull(req.getParameter(ACCOUNT_PARAMETER));
    if(accountId != null) {
        account = Convert.parseUnsignedLong(accountId);
    }
    long buyer = 0L;
    String buyerId = Convert.emptyToNull(req.getParameter(BUYER_PARAMETER));
    if(buyerId != null) {
        buyer = Convert.parseUnsignedLong(buyerId);
    }

    JsonArray aliasesJson = new JsonArray();
    CollectionWithIndex<Alias.Offer> aliasOffers = aliasService.getAliasOffers(account, buyer, firstIndex, lastIndex);
    for(Alias.Offer offer : aliasOffers) {
      Alias alias = aliasService.getAlias(offer.getId());
      aliasesJson.add(JSONData.alias(alias, offer));
    }

    JsonObject response = new JsonObject();
    response.add(ALIASES_RESPONSE, aliasesJson);
    
    if(aliasOffers.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, aliasOffers.nextIndex());
    }

    return response;
  }

}
