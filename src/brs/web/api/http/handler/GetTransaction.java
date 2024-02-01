package brs.web.api.http.handler;

import brs.Blockchain;
import brs.Transaction;
import brs.TransactionProcessor;
import brs.util.Convert;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.*;
import static brs.web.api.http.common.Parameters.FULL_HASH_PARAMETER;
import static brs.web.api.http.common.Parameters.TRANSACTION_PARAMETER;

public final class GetTransaction extends ApiServlet.JsonRequestHandler {

  private final TransactionProcessor transactionProcessor;
  private final Blockchain blockchain;

  public GetTransaction(TransactionProcessor transactionProcessor, Blockchain blockchain) {
    super(new LegacyDocTag[] {LegacyDocTag.TRANSACTIONS}, TRANSACTION_PARAMETER, FULL_HASH_PARAMETER);
    this.transactionProcessor = transactionProcessor;
    this.blockchain = blockchain;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {

    String transactionIdString = Convert.emptyToNull(req.getParameter(TRANSACTION_PARAMETER));
    String transactionFullHash = Convert.emptyToNull(req.getParameter(FULL_HASH_PARAMETER));
    if (transactionIdString == null && transactionFullHash == null) {
      return MISSING_TRANSACTION;
    }

    long transactionId = 0;
    Transaction transaction;
    try {
      if (transactionIdString != null) {
        transactionId = Convert.parseUnsignedLong(transactionIdString);
        transaction = blockchain.getTransaction(transactionId);
      } else {
        transaction = blockchain.getTransactionByFullHash(transactionFullHash);
        if (transaction == null) {
          return UNKNOWN_TRANSACTION;
        }
      }
    } catch (RuntimeException e) {
      return INCORRECT_TRANSACTION;
    }

    if (transaction == null) {
      transaction = transactionProcessor.getUnconfirmedTransaction(transactionId);
      if (transaction == null) {
        return UNKNOWN_TRANSACTION;
      }
      return JSONData.unconfirmedTransaction(transaction);
    } else {
      return JSONData.transaction(transaction, blockchain.getHeight());
    }

  }

}
