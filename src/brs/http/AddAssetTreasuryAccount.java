package brs.http;

import brs.*;
import brs.assetexchange.AssetExchange;
import brs.services.AccountService;
import brs.services.ParameterService;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.*;

public final class AddAssetTreasuryAccount extends CreateTransaction {

  private final ParameterService parameterService;
  private final Blockchain blockchain;
  private final AssetExchange assetExchange;

  public AddAssetTreasuryAccount(ParameterService parameterService,AssetExchange assetExchange, Blockchain blockchain, APITransactionManager apiTransactionManager, AccountService accountService) {
    super(new APITag[] {APITag.AE, APITag.CREATE_TRANSACTION}, apiTransactionManager, RECIPIENT_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
    this.assetExchange = assetExchange;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {

    long recipient = ParameterParser.getRecipientId(req);
    Account sender = parameterService.getSenderAccount(req);
    
    String referenceTransaction = req.getParameter(REFERENCED_TRANSACTION_FULL_HASH_PARAMETER);
    if(referenceTransaction == null) {
      return JSONResponses.missing(REFERENCED_TRANSACTION_FULL_HASH_PARAMETER);
    }
    
    Transaction transaction = blockchain.getTransactionByFullHash(referenceTransaction);
    if(transaction == null || !(transaction.getAttachment() instanceof Attachment.ColoredCoinsAssetIssuance)) {
      return JSONResponses.incorrect(REFERENCED_TRANSACTION_FULL_HASH_PARAMETER);
    }
    Asset asset = assetExchange.getAsset(transaction.getId());
    if(asset == null || asset.getAccountId() != sender.getId()) {
      return JSONResponses.incorrect(REFERENCED_TRANSACTION_FULL_HASH_PARAMETER);
    }

    return createTransaction(req, sender, recipient, 0L, Attachment.ASSET_ADD_TREASURY_ACCOUNT_ATTACHMENT);
  }

}
