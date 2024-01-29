package brs.web.api.http.handler;

import brs.Account;
import brs.Attachment;
import brs.SignumException;
import brs.services.ParameterService;
import brs.web.api.http.common.APITransactionManager;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.RECIPIENT_PARAMETER;

public final class SendMessage extends CreateTransaction {

  private final ParameterService parameterService;

  public SendMessage(ParameterService parameterService, APITransactionManager apiTransactionManager) {
    super(new LegacyDocTag[] {LegacyDocTag.MESSAGES, LegacyDocTag.CREATE_TRANSACTION}, apiTransactionManager, RECIPIENT_PARAMETER);
    this.parameterService = parameterService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
    long recipient = ParameterParser.getRecipientId(req);
    Account account = parameterService.getSenderAccount(req);
    return createTransaction(req, account, recipient, 0, Attachment.ARBITRARY_MESSAGE);
  }

}
