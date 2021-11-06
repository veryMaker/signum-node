package brs.http;

import brs.*;
import brs.fluxcapacitor.FluxValues;
import brs.services.ParameterService;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.*;

public final class MintAsset extends CreateTransaction {

  private final ParameterService parameterService;
  private final Blockchain blockchain;

  public MintAsset(ParameterService parameterService, Blockchain blockchain, APITransactionManager apiTransactionManager) {
    super(new APITag[] {APITag.AE, APITag.CREATE_TRANSACTION}, apiTransactionManager, ASSET_PARAMETER, QUANTITY_QNT_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {

    Asset asset = parameterService.getAsset(req);
    long quantityQNT = ParameterParser.getQuantityQNT(req);
    Account account = parameterService.getSenderAccount(req);
    
    if(asset.getMintable() == false) {
      return JSONResponses.incorrect("this asset is not mintable");
    }
    if(!Burst.getFluxCapacitor().getValue(FluxValues.NEXT_FORK)) {
      return JSONResponses.incorrect("minting assets is not enabled yet");
    }
    if(asset.getAccountId() != account.getId()) {
      return JSONResponses.incorrect("only the asset issuer can mint new coins");
    }

    Attachment attachment = new Attachment.ColoredCoinsAssetMint(asset.getId(), quantityQNT, blockchain.getHeight());
    return createTransaction(req, account, null, 0, attachment);

  }

}
