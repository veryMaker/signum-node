package brs.web.api.http.handler;

import brs.BurstException;
import brs.DigitalGoodsStore;
import brs.services.DGSGoodsStoreService;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.MISSING_SELLER;
import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.PURCHASES_RESPONSE;

public final class GetDGSPendingPurchases extends ApiServlet.JsonRequestHandler {

  private final DGSGoodsStoreService dgsGoodStoreService;

  public GetDGSPendingPurchases(DGSGoodsStoreService dgsGoodStoreService) {
    super(new LegacyDocTag[] {LegacyDocTag.DGS}, SELLER_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.dgsGoodStoreService = dgsGoodStoreService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    long sellerId = ParameterParser.getSellerId(req);

    if (sellerId == 0) {
      return MISSING_SELLER;
    }

    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    JsonObject response = new JsonObject();
    JsonArray purchasesJSON = new JsonArray();

    for (DigitalGoodsStore.Purchase purchase : dgsGoodStoreService.getPendingSellerPurchases(sellerId, firstIndex, lastIndex)) {
      purchasesJSON.add(JSONData.purchase(purchase));
    }

    response.add(PURCHASES_RESPONSE, purchasesJSON);
    return response;
  }

}
