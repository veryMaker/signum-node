package brs.http;

import brs.Asset;
import brs.assetexchange.AssetExchange;
import brs.services.AccountService;

import com.google.gson.JsonArray;

import java.util.Iterator;

abstract class AbstractAssetsRetrieval extends APIServlet.JsonRequestHandler {

  private final AssetExchange assetExchange;
  private final AccountService accountService;

  AbstractAssetsRetrieval(APITag[] apiTags, AssetExchange assetExchange, AccountService accountService, String... parameters) {
    super(apiTags, parameters);
    this.assetExchange = assetExchange;
    this.accountService = accountService;
  }

  JsonArray assetsToJson(Iterator<Asset> assets, int heightStart, int heightEnd, boolean skipZeroVolume) {
    final JsonArray assetsJsonArray = new JsonArray();

    while (assets.hasNext()) {
      final Asset asset = assets.next();

      int accountsCount = -1;
      int tradeCount = -1;
      int transferCount = -1;
      long circulatingSupply = assetExchange.getAssetCirculatingSupply(asset, true, false);
      long quantityBurnt = -1;
      
      long tradeVolume = assetExchange.getTradeVolume(asset.getId(), heightStart, heightEnd);
      if(tradeVolume == 0 && skipZeroVolume) {
        continue;
      }
      long highPrice = assetExchange.getHighPrice(asset.getId(), heightStart, heightEnd);
      long lowPrice = assetExchange.getLowPrice(asset.getId(), heightStart, heightEnd);
      long openPrice = assetExchange.getOpenPrice(asset.getId(), heightStart, heightEnd);
      long closePrice = assetExchange.getClosePrice(asset.getId(), heightStart, heightEnd);

      assetsJsonArray.add(JSONData.asset(asset, accountService.getAccount(asset.getAccountId()), quantityBurnt, tradeCount, transferCount, accountsCount, circulatingSupply,
          tradeVolume, highPrice, lowPrice, openPrice, closePrice));
    }

    return assetsJsonArray;
  }
}
