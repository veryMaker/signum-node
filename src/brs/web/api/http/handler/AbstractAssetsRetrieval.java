package brs.web.api.http.handler;

import brs.Asset;
import brs.assetexchange.AssetExchange;
import brs.services.AccountService;

import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonArray;

import java.util.Iterator;

abstract class AbstractAssetsRetrieval extends ApiServlet.JsonRequestHandler {

  private final AssetExchange assetExchange;
  private final AccountService accountService;

  AbstractAssetsRetrieval(LegacyDocTag[] legacyDocTags, AssetExchange assetExchange, AccountService accountService, String... parameters) {
    super(legacyDocTags, parameters);
    this.assetExchange = assetExchange;
    this.accountService = accountService;
  }

  JsonArray assetsToJson(Iterator<Asset> assets, int heightStart, int heightEnd, boolean skipZeroVolume) {
    final JsonArray assetsJsonArray = new JsonArray();

    while (assets.hasNext()) {
      final Asset asset = assets.next();

      int accountsCount = assetExchange.getAssetAccountsCount(asset, 0L, true, false);
      int tradeCount = assetExchange.getTradeCount(asset.getId());
      int transferCount = assetExchange.getTransferCount(asset.getId());
      long circulatingSupply = assetExchange.getAssetCirculatingSupply(asset, true, false);
      long quantityBurnt = accountService.getUnconfirmedAssetBalanceQNT(accountService.getNullAccount(), asset.getId());

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
