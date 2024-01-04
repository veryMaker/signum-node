package brs.web.api.http.handler;

import brs.BurstException;
import brs.Transaction;
import brs.services.ParameterService;
import brs.services.TransactionService;
import brs.util.Convert;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.TRANSACTION_BYTES_PARAMETER;
import static brs.web.api.http.common.Parameters.TRANSACTION_JSON_PARAMETER;
import static brs.web.api.http.common.ResultFields.*;

public final class ParseTransaction extends ApiServlet.JsonRequestHandler {

  private static final Logger logger = LoggerFactory.getLogger(ParseTransaction.class);

  private final ParameterService parameterService;
  private final TransactionService transactionService;

  public ParseTransaction(ParameterService parameterService, TransactionService transactionService) {
    super(new LegacyDocTag[] {LegacyDocTag.TRANSACTIONS}, TRANSACTION_BYTES_PARAMETER, TRANSACTION_JSON_PARAMETER);
    this.parameterService = parameterService;
    this.transactionService = transactionService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {

    String transactionBytes = Convert.emptyToNull(req.getParameter(TRANSACTION_BYTES_PARAMETER));
    String transactionJSON = Convert.emptyToNull(req.getParameter(TRANSACTION_JSON_PARAMETER));
    Transaction transaction = parameterService.parseTransaction(transactionBytes, transactionJSON);
    JsonObject response = JSONData.unconfirmedTransaction(transaction);
    try {
      transactionService.validate(transaction);
    } catch (BurstException.ValidationException|RuntimeException e) {
      logger.debug(e.getMessage(), e);
      response.addProperty(VALIDATE_RESPONSE, false);
      response.addProperty(ERROR_CODE_RESPONSE, 4);
      response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid transaction: " + e.toString());
      response.addProperty(ERROR_RESPONSE, e.getMessage());
    }
    response.addProperty(VERIFY_RESPONSE, transaction.verifySignature() && transactionService.verifyPublicKey(transaction));
    return response;
  }

}
