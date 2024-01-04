package brs.web.api.http.handler;

import brs.Account;
import brs.Blockchain;
import brs.BurstException;
import brs.Transaction;
import brs.services.ParameterService;
import brs.util.CollectionWithIndex;

import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.NEXT_INDEX_RESPONSE;

public final class GetAccountTransactionIds extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final Blockchain blockchain;

  public GetAccountTransactionIds(ParameterService parameterService, Blockchain blockchain) {
    super(new LegacyDocTag[]{LegacyDocTag.ACCOUNTS}, ACCOUNT_PARAMETER, TIMESTAMP_PARAMETER, TYPE_PARAMETER, SUBTYPE_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER,
        NUMBER_OF_CONFIRMATIONS_PARAMETER, INCLUDE_INDIRECT_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {

    Account account = parameterService.getAccount(req);
    int timestamp = ParameterParser.getTimestamp(req);
    int numberOfConfirmations = parameterService.getNumberOfConfirmations(req);

    byte type;
    byte subtype;
    try {
      type = Byte.parseByte(req.getParameter(TYPE_PARAMETER));
    } catch (NumberFormatException e) {
      type = -1;
    }
    try {
      subtype = Byte.parseByte(req.getParameter(SUBTYPE_PARAMETER));
    } catch (NumberFormatException e) {
      subtype = -1;
    }

    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    JsonArray transactionIds = new JsonArray();
    CollectionWithIndex<Transaction> transactions = blockchain.getTransactions(account, numberOfConfirmations, type, subtype, timestamp, firstIndex, lastIndex, parameterService.getIncludeIndirect(req));
    for (Transaction transaction : transactions) {
      transactionIds.add(transaction.getStringId());
    }

    JsonObject response = new JsonObject();
    response.add("transactionIds", transactionIds);

    if(transactions.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, transactions.nextIndex());
    }

    return response;

  }

}
