package brs.web.api.http.handler;

import brs.Account;
import brs.BurstException;
import brs.crypto.EncryptedData;
import brs.services.AccountService;
import brs.services.ParameterService;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.INCORRECT_RECIPIENT;
import static brs.web.api.http.common.Parameters.*;

public final class EncryptTo extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final AccountService accountService;

  public EncryptTo(ParameterService parameterService, AccountService accountService) {
    super(new LegacyDocTag[]{LegacyDocTag.MESSAGES}, RECIPIENT_PARAMETER, MESSAGE_TO_ENCRYPT_PARAMETER, MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER, SECRET_PHRASE_PARAMETER);
    this.parameterService = parameterService;
    this.accountService = accountService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {

    long recipientId = ParameterParser.getRecipientId(req);
    Account recipientAccount = accountService.getAccount(recipientId);
    if (recipientAccount == null || recipientAccount.getPublicKey() == null) {
      return INCORRECT_RECIPIENT;
    }

    EncryptedData encryptedData = parameterService.getEncryptedMessage(req, recipientAccount, recipientAccount.getPublicKey());
    return JSONData.encryptedData(encryptedData);

  }

}
