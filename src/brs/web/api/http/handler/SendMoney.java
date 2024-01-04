package brs.web.api.http.handler;

import brs.Account;
import brs.BurstException;
import brs.services.ParameterService;
import brs.web.api.http.common.APITransactionManager;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.AMOUNT_NQT_PARAMETER;
import static brs.web.api.http.common.Parameters.RECIPIENT_PARAMETER;

public final class SendMoney extends CreateTransaction {

  private final ParameterService parameterService;

  public SendMoney(ParameterService parameterService, APITransactionManager apiTransactionManager) {
    super(new LegacyDocTag[]{LegacyDocTag.ACCOUNTS, LegacyDocTag.TRANSACTIONS, LegacyDocTag.CREATE_TRANSACTION}, apiTransactionManager, RECIPIENT_PARAMETER, AMOUNT_NQT_PARAMETER);
    this.parameterService = parameterService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    long recipient = ParameterParser.getRecipientId(req);
    long amountNQT = ParameterParser.getAmountNQT(req);
    Account account = parameterService.getSenderAccount(req);
    return createTransaction(req, account, recipient, amountNQT);
  }

}
