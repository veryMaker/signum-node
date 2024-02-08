package brs.web.api.http.handler;

import brs.*;
import brs.services.ParameterService;
import brs.web.api.http.common.APITransactionManager;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.ERROR_CODE_RESPONSE;
import static brs.web.api.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE;

public final class SendMoneySubscription extends CreateTransaction {

  private final ParameterService parameterService;
  private final Blockchain blockchain;

  public SendMoneySubscription(ParameterService parameterService, Blockchain blockchain, APITransactionManager apiTransactionManager) {
    super(new LegacyDocTag[] {LegacyDocTag.TRANSACTIONS, LegacyDocTag.CREATE_TRANSACTION}, apiTransactionManager, RECIPIENT_PARAMETER, AMOUNT_NQT_PARAMETER, FREQUENCY_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
    Account sender = parameterService.getSenderAccount(req);
    Long recipient = ParameterParser.getRecipientId(req);
    long amountNQT = ParameterParser.getAmountNQT(req);

    int frequency;
    try {
      frequency = Integer.parseInt(req.getParameter(FREQUENCY_PARAMETER));
    }
    catch(Exception e) {
      JsonObject response = new JsonObject();
      response.addProperty(ERROR_CODE_RESPONSE, 4);
      response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid or missing frequency parameter");
      return response;
    }

    if(frequency < Constants.SIGNUM_SUBSCRIPTION_MIN_FREQ ||
       frequency > Constants.SIGNUM_SUBSCRIPTION_MAX_FREQ) {
      JsonObject response = new JsonObject();
      response.addProperty(ERROR_CODE_RESPONSE, 4);
      response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid frequency amount");
      return response;
    }

    Attachment.AdvancedPaymentSubscriptionSubscribe attachment = new Attachment.AdvancedPaymentSubscriptionSubscribe(frequency, blockchain.getHeight());

    return createTransaction(req, sender, recipient, amountNQT, attachment);
  }
}
