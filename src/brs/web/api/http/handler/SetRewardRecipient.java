package brs.web.api.http.handler;

import brs.Account;
import brs.Attachment;
import brs.Blockchain;
import brs.SignumException;
import brs.services.AccountService;
import brs.services.ParameterService;
import brs.web.api.http.common.APITransactionManager;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.RECIPIENT_PARAMETER;
import static brs.web.api.http.common.ResultFields.ERROR_CODE_RESPONSE;
import static brs.web.api.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE;

public final class SetRewardRecipient extends CreateTransaction {

  private final ParameterService parameterService;
  private final Blockchain blockchain;
  private final AccountService accountService;

  public SetRewardRecipient(ParameterService parameterService, Blockchain blockchain, AccountService accountService, APITransactionManager apiTransactionManager) {
    super(new LegacyDocTag[] {LegacyDocTag.ACCOUNTS, LegacyDocTag.MINING, LegacyDocTag.CREATE_TRANSACTION}, apiTransactionManager, RECIPIENT_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
    this.accountService = accountService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
    final Account account = parameterService.getSenderAccount(req);
    Long recipient = ParameterParser.getRecipientId(req);
    Account recipientAccount = accountService.getAccount(recipient);
    if (recipientAccount == null || recipientAccount.getPublicKey() == null) {
      JsonObject response = new JsonObject();
      response.addProperty(ERROR_CODE_RESPONSE, 8);
      response.addProperty(ERROR_DESCRIPTION_RESPONSE, "recipient account does not have public key");
      return response;
    }
    Attachment attachment = new Attachment.SignaMiningRewardRecipientAssignment(blockchain.getHeight());
    return createTransaction(req, account, recipient, 0, attachment);
  }

}
