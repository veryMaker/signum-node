package brs.web.api.http.handler;

import brs.Transaction;
import brs.TransactionProcessor;
import brs.services.IndirectIncomingService;
import brs.services.ParameterService;
import brs.util.Convert;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static brs.web.api.http.common.JSONResponses.INCORRECT_ACCOUNT;
import static brs.web.api.http.common.Parameters.ACCOUNT_PARAMETER;
import static brs.web.api.http.common.Parameters.INCLUDE_INDIRECT_PARAMETER;
import static brs.web.api.http.common.ResultFields.UNCONFIRMED_TRANSACTIONS_RESPONSE;

public final class GetUnconfirmedTransactions extends ApiServlet.JsonRequestHandler {

  private final TransactionProcessor transactionProcessor;
  private final IndirectIncomingService indirectIncomingService;
  private final ParameterService parameterService;

  public GetUnconfirmedTransactions(TransactionProcessor transactionProcessor, IndirectIncomingService indirectIncomingService, ParameterService parameterService) {
    super(new LegacyDocTag[]{LegacyDocTag.TRANSACTIONS, LegacyDocTag.ACCOUNTS}, ACCOUNT_PARAMETER, INCLUDE_INDIRECT_PARAMETER);
    this.transactionProcessor = transactionProcessor;
    this.indirectIncomingService = indirectIncomingService;
    this.parameterService = parameterService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {
    final String accountIdString = Convert.emptyToNull(req.getParameter(ACCOUNT_PARAMETER));
    boolean includeIndirect = parameterService.getIncludeIndirect(req);

    long accountId = 0;

    if (accountIdString != null) {
      try {
        accountId = Convert.parseAccountId(accountIdString);
      } catch (RuntimeException e) {
        return INCORRECT_ACCOUNT;
      }
    }

    final List<Transaction> unconfirmedTransactions = transactionProcessor.getAllUnconfirmedTransactions();

    final JsonArray transactions = new JsonArray();

    for (Transaction transaction : unconfirmedTransactions) {
      if (accountId == 0
              || (accountId == transaction.getSenderId() || accountId == transaction.getRecipientId())
              || (includeIndirect && indirectIncomingService.isIndirectlyReceiving(transaction, accountId))) {
        transactions.add(JSONData.unconfirmedTransaction(transaction));
      }
    }

    final JsonObject response = new JsonObject();

    response.add(UNCONFIRMED_TRANSACTIONS_RESPONSE, transactions);

    return response;
  }

}
