package brs.web.api.http.handler;

import brs.BurstException;
import brs.DigitalGoodsStore;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import brs.web.api.http.common.Parameters;
import brs.services.DGSGoodsStoreService;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.GOODS_RESPONSE;

public final class GetDGSGoods extends ApiServlet.JsonRequestHandler {

  private final DGSGoodsStoreService digitalGoodsStoreService;

  public GetDGSGoods(DGSGoodsStoreService digitalGoodsStoreService) {
    super(new LegacyDocTag[] {LegacyDocTag.DGS}, SELLER_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, IN_STOCK_ONLY_PARAMETER);
    this.digitalGoodsStoreService = digitalGoodsStoreService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    long sellerId = ParameterParser.getSellerId(req);
    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);
    boolean inStockOnly = !Parameters.isFalse(req.getParameter(IN_STOCK_ONLY_PARAMETER));

    JsonObject response = new JsonObject();
    JsonArray goodsJSON = new JsonArray();
    response.add(GOODS_RESPONSE, goodsJSON);

    Collection<DigitalGoodsStore.Goods> goods = null;
    if (sellerId == 0) {
      if (inStockOnly) {
        goods = digitalGoodsStoreService.getGoodsInStock(firstIndex, lastIndex);
      } else {
        goods = digitalGoodsStoreService.getAllGoods(firstIndex, lastIndex);
      }
    } else {
      goods = digitalGoodsStoreService.getSellerGoods(sellerId, inStockOnly, firstIndex, lastIndex);
    }
    for (DigitalGoodsStore.Goods good : goods) {
      goodsJSON.add(JSONData.goods(good));
    }

    return response;
  }

}
