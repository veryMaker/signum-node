package brs.http;

import brs.Asset;
import brs.Burst;
import brs.BurstException;
import brs.assetexchange.AssetExchange;
import brs.services.AccountService;
import brs.services.ParameterService;
import brs.util.Convert;

import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.*;

public final class GetAsset extends APIServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final AssetExchange assetExchange;
  private final AccountService accountService;

  GetAsset(ParameterService parameterService, AssetExchange assetExchange, AccountService accountService) {
    super(new APITag[]{APITag.AE}, ASSET_PARAMETER, QUANTITY_MININUM_QNT_PARAMETER, HEIGHT_START_PARAMETER, HEIGHT_END_PARAMETER);
    this.parameterService = parameterService;
    this.assetExchange = assetExchange;
    this.accountService = accountService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    final Asset asset = parameterService.getAsset(req);
    long minimumQuantity = Convert.parseUnsignedLong(req.getParameter(QUANTITY_MININUM_QNT_PARAMETER));

    int tradeCount = assetExchange.getTradeCount(asset.getId());
    int transferCount = assetExchange.getTransferCount(asset.getId());
    int accountsCount = assetExchange.getAssetAccountsCount(asset, minimumQuantity, true, false);
    long circulatingSupply = assetExchange.getAssetCirculatingSupply(asset, true, false);
    
    long quantityBurnt = accountService.getUnconfirmedAssetBalanceQNT(accountService.getNullAccount(), asset.getId());
    
    int heightEnd = Burst.getBlockchain().getHeight();
    // default is one day window
    int heightStart = heightEnd - 360;
    
    String heightStartString = Convert.emptyToNull(req.getParameter(HEIGHT_START_PARAMETER));
    if(heightStartString != null) {
      heightStart = Integer.getInteger(heightStartString);
    }

    String heightEndString = Convert.emptyToNull(req.getParameter(HEIGHT_END_PARAMETER));
    if(heightEndString != null) {
      heightEnd = Integer.getInteger(heightEndString);
    }
    
    long tradeVolume = assetExchange.getTradeVolume(asset.getId(), heightStart, heightEnd);
    long highPrice = assetExchange.getHighPrice(asset.getId(), heightStart, heightEnd);
    long lowPrice = assetExchange.getLowPrice(asset.getId(), heightStart, heightEnd);
    long openPrice = assetExchange.getOpenPrice(asset.getId(), heightStart);
    long closePrice = assetExchange.getOpenPrice(asset.getId(), heightEnd);

    return JSONData.asset(asset, quantityBurnt, tradeCount, transferCount, accountsCount, circulatingSupply,
        tradeVolume, highPrice, lowPrice, openPrice, closePrice);
  }

}
