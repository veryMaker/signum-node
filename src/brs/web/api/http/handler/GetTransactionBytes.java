package brs.web.api.http.handler;

import brs.Blockchain;
import brs.Transaction;
import brs.TransactionProcessor;
import brs.util.Convert;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.*;
import static brs.web.api.http.common.Parameters.TRANSACTION_PARAMETER;

public final class GetTransactionBytes extends ApiServlet.JsonRequestHandler {

  private final Blockchain blockchain;
  private final TransactionProcessor transactionProcessor;

  public GetTransactionBytes(Blockchain blockchain, TransactionProcessor transactionProcessor) {
    super(new LegacyDocTag[] {LegacyDocTag.TRANSACTIONS}, TRANSACTION_PARAMETER);
    this.blockchain = blockchain;
    this.transactionProcessor = transactionProcessor;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {

    String transactionValue = req.getParameter(TRANSACTION_PARAMETER);
    if (transactionValue == null) {
      return MISSING_TRANSACTION;
    }

    long transactionId;
    Transaction transaction;
    try {
      transactionId = Convert.parseUnsignedLong(transactionValue);
    } catch (RuntimeException e) {
      return INCORRECT_TRANSACTION;
    }

    transaction = blockchain.getTransaction(transactionId);
    JsonObject response = new JsonObject();
    if (transaction == null) {
      transaction = transactionProcessor.getUnconfirmedTransaction(transactionId);
      if (transaction == null) {
        return UNKNOWN_TRANSACTION;
      }
    } else {
      response.addProperty("confirmations", blockchain.getHeight() - transaction.getHeight());
    }

    response.addProperty("transactionBytes", Convert.toHexString(transaction.getBytes()));
    response.addProperty("unsignedTransactionBytes", Convert.toHexString(transaction.getUnsignedBytes()));

    return response;
  }

}
