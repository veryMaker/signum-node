package brs.http;

import static brs.http.JSONResponses.INCORRECT_TRANSACTION;
import static brs.http.JSONResponses.MISSING_ACCOUNT;
import static brs.http.JSONResponses.MISSING_TRANSACTION;
import static brs.http.JSONResponses.UNKNOWN_TRANSACTION;
import static brs.http.common.Parameters.ACCOUNT_PARAMETER;
import static brs.http.common.Parameters.TRANSACTION_PARAMETER;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonElement;

import brs.Blockchain;
import brs.Burst;
import brs.IndirectIncoming;
import brs.util.Convert;

final class GetTransactionIndirect extends APIServlet.JsonRequestHandler {

  private final Blockchain blockchain;

  GetTransactionIndirect(Blockchain blockchain) {
    super(new APITag[] {APITag.TRANSACTIONS}, TRANSACTION_PARAMETER, ACCOUNT_PARAMETER);
    this.blockchain = blockchain;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {

    String transactionIdString = Convert.emptyToNull(req.getParameter(TRANSACTION_PARAMETER));
    String accountIdString = Convert.emptyToNull(req.getParameter(ACCOUNT_PARAMETER));
    if (transactionIdString == null) {
      return MISSING_TRANSACTION;
    }
    if (accountIdString == null) {
      return MISSING_ACCOUNT;
    }

    long transactionId = 0;
    long accountId = 0;
    try {
      transactionId = Convert.parseUnsignedLong(transactionIdString);
      accountId = Convert.parseUnsignedLong(accountIdString);
    } catch (RuntimeException e) {
      return INCORRECT_TRANSACTION;
    }
    
    IndirectIncoming indirect = Burst.getStores().getIndirectIncomingStore().getIndirectIncoming(accountId, transactionId);

    if (indirect == null) {
      return UNKNOWN_TRANSACTION;
    }
    return JSONData.indirect(indirect, blockchain.getHeight());

  }

}
