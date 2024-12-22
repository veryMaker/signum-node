package brs.web.api.http.handler;

import brs.Asset;
import brs.Signum;
import brs.SignumException;
import brs.assetexchange.AssetExchange;
import brs.fluxcapacitor.FluxValues;
import brs.services.AccountService;
import brs.services.ParameterService;
import brs.util.Convert;

import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.*;

public final class GetAsset extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final AssetExchange assetExchange;
  private final AccountService accountService;

  public GetAsset(ParameterService parameterService, AssetExchange assetExchange, AccountService accountService) {
    super(new LegacyDocTag[]{LegacyDocTag.AE}, ASSET_PARAMETER, QUANTITY_MININUM_QNT_PARAMETER, HEIGHT_START_PARAMETER, HEIGHT_END_PARAMETER);
    this.parameterService = parameterService;
    this.assetExchange = assetExchange;
    this.accountService = accountService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
    final Asset asset = parameterService.getAsset(req);
    long minimumQuantity = Convert.parseUnsignedLong(req.getParameter(QUANTITY_MININUM_QNT_PARAMETER));

    int tradeCount = assetExchange.getTradeCount(asset.getId());
    int transferCount = assetExchange.getTransferCount(asset.getId());
    boolean unconfirmed = !Signum.getFluxCapacitor().getValue(FluxValues.DISTRIBUTION_FIX);
    int accountsCount = assetExchange.getAssetAccountsCount(asset, minimumQuantity, true, unconfirmed);
    long circulatingSupply = assetExchange.getAssetCirculatingSupply(asset, true, unconfirmed);

    long quantityBurnt = accountService.getUnconfirmedAssetBalanceQNT(accountService.getNullAccount(), asset.getId());

    int heightEnd = Signum.getBlockchain().getHeight();
    // default is one day window
    int heightStart = heightEnd - 360;

    String heightStartString = Convert.emptyToNull(req.getParameter(HEIGHT_START_PARAMETER));
    if(heightStartString != null) {
      heightStart = Integer.parseInt(heightStartString);
    }

    String heightEndString = Convert.emptyToNull(req.getParameter(HEIGHT_END_PARAMETER));
    if(heightEndString != null) {
      heightEnd = Integer.parseInt(heightEndString);
    }

    long tradeVolume = assetExchange.getTradeVolume(asset.getId(), heightStart, heightEnd);
    long highPrice = assetExchange.getHighPrice(asset.getId(), heightStart, heightEnd);
    long lowPrice = assetExchange.getLowPrice(asset.getId(), heightStart, heightEnd);
    long openPrice = assetExchange.getOpenPrice(asset.getId(), heightStart, heightEnd);
    long closePrice = assetExchange.getClosePrice(asset.getId(), heightStart, heightEnd);

    return JSONData.asset(asset, accountService.getAccount(asset.getAccountId()),
        quantityBurnt, tradeCount, transferCount, accountsCount, circulatingSupply,
        tradeVolume, highPrice, lowPrice, openPrice, closePrice);
  }

}
