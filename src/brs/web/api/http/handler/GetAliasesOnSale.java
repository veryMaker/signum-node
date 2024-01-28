package brs.web.api.http.handler;

import brs.Alias;
import brs.SignumException;
import brs.services.AliasService;
import brs.util.CollectionWithIndex;
import brs.util.Convert;

import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.ALIASES_RESPONSE;
import static brs.web.api.http.common.ResultFields.NEXT_INDEX_RESPONSE;

public final class GetAliasesOnSale extends ApiServlet.JsonRequestHandler {

  private final AliasService aliasService;

  public GetAliasesOnSale(AliasService aliasService) {
    super(new LegacyDocTag[]{LegacyDocTag.ALIASES}, ACCOUNT_PARAMETER, BUYER_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.aliasService = aliasService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
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
      Alias tld = aliasService.getTLD(alias.getTLD());
      aliasesJson.add(JSONData.alias(alias, tld, offer, 0));
    }

    JsonObject response = new JsonObject();
    response.add(ALIASES_RESPONSE, aliasesJson);

    if(aliasOffers.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, aliasOffers.nextIndex());
    }

    return response;
  }

}
