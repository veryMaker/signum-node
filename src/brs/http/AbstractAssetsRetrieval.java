package brs.http;

import brs.Asset;
import brs.assetexchange.AssetExchange;
import com.google.gson.JsonArray;

import java.util.Iterator;

abstract class AbstractAssetsRetrieval extends APIServlet.JsonRequestHandler {

  private final AssetExchange assetExchange;

  AbstractAssetsRetrieval(APITag[] apiTags, AssetExchange assetExchange, String... parameters) {
    super(apiTags, parameters);
    this.assetExchange = assetExchange;
  }

  JsonArray assetsToJson(Iterator<Asset> assets, int heightStart, int heightEnd) {
    final JsonArray assetsJsonArray = new JsonArray();

    while (assets.hasNext()) {
      final Asset asset = assets.next();

      int accountsCount = -1;
      int tradeCount = -1;
      int transferCount = -1;
      long circulatingSupply = assetExchange.getAssetCirculatingSupply(asset, true, false);
      long quantityBurnt = -1;
      
      long tradeVolume = assetExchange.getTradeVolume(asset.getId(), heightStart, heightEnd);
      long highPrice = assetExchange.getHighPrice(asset.getId(), heightStart, heightEnd);
      long lowPrice = assetExchange.getLowPrice(asset.getId(), heightStart, heightEnd);
      long openPrice = assetExchange.getOpenPrice(asset.getId(), heightStart, heightEnd);
      long closePrice = assetExchange.getClosePrice(asset.getId(), heightStart, heightEnd);

      assetsJsonArray.add(JSONData.asset(asset, quantityBurnt, tradeCount, transferCount, accountsCount, circulatingSupply,
          tradeVolume, highPrice, lowPrice, openPrice, closePrice));
    }

    return assetsJsonArray;
  }
}
