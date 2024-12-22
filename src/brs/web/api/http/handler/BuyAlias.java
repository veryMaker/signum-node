package brs.web.api.http.handler;

import brs.*;
import brs.services.AliasService;
import brs.services.ParameterService;
import brs.web.api.http.common.APITransactionManager;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.INCORRECT_ALIAS_NOTFORSALE;
import static brs.web.api.http.common.Parameters.*;

public final class BuyAlias extends CreateTransaction {

  private final ParameterService parameterService;
  private final AliasService aliasService;
  private final Blockchain blockchain;

  public BuyAlias(ParameterService parameterService, Blockchain blockchain, AliasService aliasService, APITransactionManager apiTransactionManager) {
    super(new LegacyDocTag[]{LegacyDocTag.ALIASES, LegacyDocTag.CREATE_TRANSACTION}, apiTransactionManager, ALIAS_PARAMETER, ALIAS_NAME_PARAMETER, TLD_PARAMETER, AMOUNT_NQT_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
    this.aliasService = aliasService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
    Account buyer = parameterService.getSenderAccount(req);
    Alias alias = parameterService.getAlias(req);
    long amountNQT = ParameterParser.getAmountNQT(req);

    if (aliasService.getOffer(alias) == null) {
      return INCORRECT_ALIAS_NOTFORSALE;
    }

    long sellerId = alias.getAccountId();
    Attachment attachment = new Attachment.MessagingAliasBuy(alias.getId(), alias.getAliasName(), blockchain.getHeight());
    return createTransaction(req, buyer, sellerId, amountNQT, attachment);
  }
}
