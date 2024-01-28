package brs.web.api.http.handler;

import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.*;
import javax.servlet.http.HttpServletRequest;

import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import brs.Account;
import brs.Block;
import brs.Blockchain;
import brs.Signum;
import brs.BurstException;
import brs.Constants;
import brs.Generator;
import brs.services.AccountService;
import brs.services.ParameterService;
import brs.util.Convert;
import signumj.crypto.SignumCrypto;

public final class GetAccount extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final Blockchain blockchain;
  private final AccountService accountService;
  private final Generator generator;

  public GetAccount(ParameterService parameterService, AccountService accountService, Blockchain blockchain, Generator generator) {
    super(new LegacyDocTag[] {LegacyDocTag.ACCOUNTS}, ACCOUNT_PARAMETER, HEIGHT_PARAMETER, GET_COMMITTED_AMOUNT_PARAMETER, ESTIMATE_COMMITMENT_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
    this.accountService = accountService;
    this.generator = generator;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {

    Account account = parameterService.getAccount(req);

    JsonObject response = JSONData.accountBalance(account);

    int height = parameterService.getHeight(req);
    if(height < 0) {
      height = blockchain.getHeight();
    }

    if(parameterService.getAmountCommitted(req)) {
      long committedAmount = Signum.getBlockchain().getCommittedAmount(account.getId(), height+Constants.COMMITMENT_WAIT, height, null);
      response.addProperty(COMMITTED_NQT_RESPONSE, Convert.toUnsignedLong(committedAmount));
    }

    if(parameterService.getEstimateCommitment(req)) {
      Block block = blockchain.getBlockAtHeight(height);
      long commitment = generator.estimateCommitment(account.getId(), block);
      response.addProperty(COMMITMENT_NQT_RESPONSE, Convert.toUnsignedLong(commitment));
    }

    JSONData.putAccount(response, ACCOUNT_RESPONSE, account.getId());

    if (account.getPublicKey() != null) {
      if(!Convert.checkAllZero(account.getPublicKey())) {
        response.addProperty(ACCOUNT_RESPONSE + "RSExtended", SignumCrypto.getInstance().getAddressFromPublic(account.getPublicKey()).getExtendedAddress());
      }
      response.addProperty(PUBLIC_KEY_RESPONSE, Convert.toHexString(account.getPublicKey()));
    }
    if (account.getName() != null) {
      response.addProperty(NAME_RESPONSE, account.getName());
    }
    if (account.getDescription() != null) {
      response.addProperty(DESCRIPTION_RESPONSE, account.getDescription());
    }

    response.addProperty(IS_AT_RESPONSE, account.isAT());
    response.addProperty(IS_SECURED_RESPONSE, account.getPublicKey() != null);

    if(height == blockchain.getHeight()) {
      // Only if the height is the latest as we don't handle past asset balances.
      // Returning assets here is needed by the classic wallet, so we keep it.
      JsonArray assetBalances = new JsonArray();
      JsonArray unconfirmedAssetBalances = new JsonArray();

      for (Account.AccountAsset accountAsset : accountService.getAssets(account.getId(), 0, -1)) {
        JsonObject assetBalance = new JsonObject();
        assetBalance.addProperty(ASSET_RESPONSE, Convert.toUnsignedLong(accountAsset.getAssetId()));
        assetBalance.addProperty(BALANCE_QNT_RESPONSE, String.valueOf(accountAsset.getQuantityQNT()));
        assetBalances.add(assetBalance);
        JsonObject unconfirmedAssetBalance = new JsonObject();
        unconfirmedAssetBalance.addProperty(ASSET_RESPONSE, Convert.toUnsignedLong(accountAsset.getAssetId()));
        unconfirmedAssetBalance.addProperty(UNCONFIRMED_BALANCE_QNT_RESPONSE, String.valueOf(accountAsset.getUnconfirmedQuantityQNT()));
        unconfirmedAssetBalances.add(unconfirmedAssetBalance);
      }

      if (!assetBalances.isEmpty()) {
        response.add(ASSET_BALANCES_RESPONSE, assetBalances);
      }
      if (!unconfirmedAssetBalances.isEmpty()) {
        response.add(UNCONFIRMED_ASSET_BALANCES_RESPONSE, unconfirmedAssetBalances);
      }
    }

    return response;
  }

}
