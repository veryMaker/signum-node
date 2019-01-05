package brs.http;

import brs.Transaction;
import brs.TransactionProcessor;
import brs.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static brs.http.JSONResponses.INCORRECT_ACCOUNT;
import static brs.http.common.Parameters.ACCOUNT_PARAMETER;
import static brs.http.common.ResultFields.UNCONFIRMED_TRANSACTIONS_RESPONSE;

final class GetUnconfirmedTransactions extends APIServlet.APIRequestHandler {

  private final TransactionProcessor transactionProcessor;

  GetUnconfirmedTransactions(TransactionProcessor transactionProcessor) {
    super(new APITag[]{APITag.TRANSACTIONS, APITag.ACCOUNTS}, ACCOUNT_PARAMETER);
    this.transactionProcessor = transactionProcessor;
  }

  @Override
  JSONStreamAware processRequest(HttpServletRequest req) {
    final String accountIdString = Convert.emptyToNull(req.getParameter(ACCOUNT_PARAMETER));

    long accountId = 0;

    if (accountIdString != null) {
      try {
        accountId = Convert.parseAccountId(accountIdString);
      } catch (RuntimeException e) {
        return INCORRECT_ACCOUNT;
      }
    }

    final List<Transaction> unconfirmedTransactions = transactionProcessor.getAllUnconfirmedTransactions();

    final JSONArray transactions = new JSONArray();

    for (Transaction transaction : unconfirmedTransactions) {
      if (accountId != 0 && !(accountId == transaction.getSenderId() || accountId == transaction.getRecipientId())) {
        continue;
      }
      transactions.add(JSONData.unconfirmedTransaction(transaction));
    }

    final JSONObject response = new JSONObject();

    response.put(UNCONFIRMED_TRANSACTIONS_RESPONSE, transactions);

    return response;
  }

}
