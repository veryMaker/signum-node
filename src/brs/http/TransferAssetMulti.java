package brs.http;

import static brs.http.JSONResponses.NOT_ENOUGH_ASSETS;
import static brs.http.common.Parameters.AMOUNT_NQT_PARAMETER;
import static brs.http.common.Parameters.ASSET_IDS_PARAMETER;
import static brs.http.common.Parameters.QUANTITIES_QNT_PARAMETER;
import static brs.http.common.Parameters.RECIPIENT_PARAMETER;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonElement;

import brs.Account;
import brs.Asset;
import brs.Attachment;
import brs.Blockchain;
import brs.Burst;
import brs.BurstException;
import brs.Constants;
import brs.fluxcapacitor.FluxValues;
import brs.services.AccountService;
import brs.services.ParameterService;
import brs.util.Convert;

public final class TransferAssetMulti extends CreateTransaction {

  private final ParameterService parameterService;
  private final Blockchain blockchain;
  private final AccountService accountService;

  public TransferAssetMulti(ParameterService parameterService, Blockchain blockchain, APITransactionManager apiTransactionManager, AccountService accountService) {
    super(new APITag[] {APITag.AE, APITag.CREATE_TRANSACTION}, apiTransactionManager, RECIPIENT_PARAMETER, ASSET_IDS_PARAMETER, QUANTITIES_QNT_PARAMETER, AMOUNT_NQT_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
    this.accountService = accountService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {

    long recipient = ParameterParser.getRecipientId(req);
    Account account = parameterService.getSenderAccount(req);
    
    String assetIdsString = Convert.emptyToNull(req.getParameter(ASSET_IDS_PARAMETER));
    String quantitiesString = Convert.emptyToNull(req.getParameter(QUANTITIES_QNT_PARAMETER));

    if(assetIdsString == null) {
      return JSONResponses.missing(ASSET_IDS_PARAMETER);
    }
    if(quantitiesString == null) {
      return JSONResponses.missing(QUANTITIES_QNT_PARAMETER);
    }

    String[] assetIdsArray = assetIdsString.split(";", Constants.MAX_MULTI_ASSET_IDS);
    ArrayList<Long> assetIds = new ArrayList<>();
    String[] quantitiesArray = quantitiesString.split(";", Constants.MAX_MULTI_ASSET_IDS);
    ArrayList<Long> quantitiesQNT = new ArrayList<>();
    
    if(assetIdsArray.length == 0 || assetIdsArray.length > Constants.MAX_MULTI_ASSET_IDS) {
      return JSONResponses.incorrect(ASSET_IDS_PARAMETER);
    }
    if(assetIdsArray.length != quantitiesArray.length) {
      return JSONResponses.incorrect(QUANTITIES_QNT_PARAMETER);
    }

    for(String assetIdString : assetIdsArray) {
      long assetId = Convert.parseUnsignedLong(assetIdString);
      Asset asset = Burst.getStores().getAssetStore().getAsset(assetId);
      if(asset == null || assetIds.contains(assetId)) {
        return JSONResponses.incorrect(ASSET_IDS_PARAMETER);
      }
      assetIds.add(assetId);
    }

    for(String quantityString : quantitiesArray) {
      long quantityQNT = Long.parseLong(quantityString);
      if(quantityQNT <= 0L) {
        return JSONResponses.incorrect(QUANTITIES_QNT_PARAMETER);
      }
      
      long assetId = assetIds.get(quantitiesQNT.size());
      
      long assetBalance = accountService.getUnconfirmedAssetBalanceQNT(account, assetId);
      if (assetBalance < 0 || quantityQNT > assetBalance) {
        return NOT_ENOUGH_ASSETS;
      }
      quantitiesQNT.add(quantityQNT);
    }

    
    long amountNQT = 0L;
    String amountValueNQT = Convert.emptyToNull(req.getParameter(AMOUNT_NQT_PARAMETER));
    if (amountValueNQT != null) {
      try {
        amountNQT = Long.parseLong(amountValueNQT);
      } catch (RuntimeException e) {
        return JSONResponses.incorrect(AMOUNT_NQT_PARAMETER);
      }
      if (amountNQT < 0 || amountNQT >= Constants.MAX_BALANCE_NQT) {
        return JSONResponses.incorrect(AMOUNT_NQT_PARAMETER);
      }
      else if (!Burst.getFluxCapacitor().getValue(FluxValues.SMART_TOKEN)) {
        return JSONResponses.incorrect(AMOUNT_NQT_PARAMETER);
      }
    }

    Attachment attachment = new Attachment.ColoredCoinsAssetMultiTransfer(assetIds, quantitiesQNT, blockchain.getHeight());
    return createTransaction(req, account, recipient, amountNQT, attachment);

  }

}
