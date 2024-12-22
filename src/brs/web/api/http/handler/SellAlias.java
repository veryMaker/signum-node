package brs.web.api.http.handler;

import brs.*;
import brs.services.ParameterService;
import brs.util.Convert;
import brs.web.api.http.common.APITransactionManager;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterException;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.*;
import static brs.web.api.http.common.Parameters.*;

public final class SellAlias extends CreateTransaction {

  private final ParameterService parameterService;
  private final Blockchain blockchain;

  public SellAlias(ParameterService parameterService, Blockchain blockchain, APITransactionManager apiTransactionManager) {
    super(new LegacyDocTag[] {LegacyDocTag.ALIASES, LegacyDocTag.CREATE_TRANSACTION}, apiTransactionManager, ALIAS_PARAMETER, ALIAS_NAME_PARAMETER, TLD_PARAMETER, RECIPIENT_PARAMETER, PRICE_NQT_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
    Alias alias = parameterService.getAlias(req);
    Account owner = parameterService.getSenderAccount(req);

    String priceValueNQT = Convert.emptyToNull(req.getParameter(PRICE_NQT_PARAMETER));
    if (priceValueNQT == null) {
      return MISSING_PRICE;
    }
    long priceNQT;
    try {
      priceNQT = Long.parseLong(priceValueNQT);
    } catch (RuntimeException e) {
      return INCORRECT_PRICE;
    }
    if (priceNQT < 0 || priceNQT > Constants.MAX_BALANCE_NQT) {
      throw new ParameterException(INCORRECT_PRICE);
    }

    String recipientValue = Convert.emptyToNull(req.getParameter(RECIPIENT_PARAMETER));
    long recipientId = 0;
    if (recipientValue != null) {
      try {
        recipientId = Convert.parseAccountId(recipientValue);
      } catch (RuntimeException e) {
        return INCORRECT_RECIPIENT;
      }
      if (recipientId == 0) {
        return INCORRECT_RECIPIENT;
      }
    }

    if (alias.getAccountId() != owner.getId()) {
      return INCORRECT_ALIAS_OWNER;
    }

    Attachment attachment = new Attachment.MessagingAliasSell(alias.getId(), alias.getAliasName(), priceNQT, blockchain.getHeight());
    return createTransaction(req, owner, recipientId, 0, attachment);
  }
}
