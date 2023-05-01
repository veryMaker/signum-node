package brs.http;

import brs.*;
import brs.assetexchange.AssetExchange;
import brs.fluxcapacitor.FluxValues;
import brs.services.ParameterService;
import brs.util.Convert;

import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.*;

public final class TransferAssetOwnership extends CreateTransaction {

  private final ParameterService parameterService;
  private final Blockchain blockchain;
  private final AssetExchange assetExchange;

  public TransferAssetOwnership(ParameterService parameterService, Blockchain blockchain,
      APITransactionManager apiTransactionManager, AssetExchange assetExchange) {
    super(new APITag[] {APITag.AE, APITag.CREATE_TRANSACTION}, apiTransactionManager, RECIPIENT_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
    this.assetExchange = assetExchange;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {

    Account account = parameterService.getSenderAccount(req);
    long recipientId = ParameterParser.getRecipientId(req);

    String fullHashReference = Convert.emptyToNull(req.getParameter(REFERENCED_TRANSACTION_FULL_HASH_PARAMETER));
    Transaction assetIssuanceTransaction = blockchain.getTransactionByFullHash(fullHashReference);

    if(assetIssuanceTransaction == null) {
      return JSONResponses.incorrect(REFERENCED_TRANSACTION_FULL_HASH_PARAMETER);
    }

    Asset asset = assetExchange.getAsset(assetIssuanceTransaction.getId());
    if(asset == null) {
      return JSONResponses.incorrect(REFERENCED_TRANSACTION_FULL_HASH_PARAMETER, "reference transaction full hash is not an asset issuance transaction");
    }

    if(asset.getAccountId() != account.getId()) {
      return JSONResponses.incorrect(REFERENCED_TRANSACTION_FULL_HASH_PARAMETER, "sender is not the asset current owner");
    }
    if(!Burst.getFluxCapacitor().getValue(FluxValues.PK_FREEZE2)) {
      return JSONResponses.incorrect(REFERENCED_TRANSACTION_FULL_HASH_PARAMETER, "ownership transfer is not enabled yet");
    }

    Attachment attachment = Attachment.COLORED_COINS_ASSET_TRANSFER_OWNERSHIP;
    return createTransaction(req, account, recipientId, 0L, attachment);
  }

}
