package brs.web.api.http.handler;

import brs.Attachment;
import brs.Attachment.AdvancedPaymentSubscriptionPayment;
import brs.Blockchain;
import brs.Signum;
import brs.Transaction;
import brs.TransactionType;
import brs.util.CollectionWithIndex;
import brs.util.Convert;

import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.FIRST_INDEX_PARAMETER;
import static brs.web.api.http.common.Parameters.LAST_INDEX_PARAMETER;
import static brs.web.api.http.common.Parameters.SUBSCRIPTION_PARAMETER;
import static brs.web.api.http.common.ResultFields.ERROR_CODE_RESPONSE;
import static brs.web.api.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE;
import static brs.web.api.http.common.ResultFields.NEXT_INDEX_RESPONSE;
import static brs.web.api.http.common.ResultFields.TRANSACTIONS_RESPONSE;

public final class GetSubscriptionPayments extends ApiServlet.JsonRequestHandler {

  private final Blockchain blockchain;

  public GetSubscriptionPayments(Blockchain blockchain) {
    super(new LegacyDocTag[] {LegacyDocTag.ACCOUNTS}, SUBSCRIPTION_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.blockchain = blockchain;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {
    long subscriptionId;
    try {
      subscriptionId = Convert.parseUnsignedLong(Convert.emptyToNull(req.getParameter(SUBSCRIPTION_PARAMETER)));
    }
    catch(Exception e) {
      JsonObject response = new JsonObject();
      response.addProperty(ERROR_CODE_RESPONSE, 3);
      response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid or not specified subscription");
      return response;
    }

    Transaction tx = blockchain.getTransaction(subscriptionId);
    if(tx == null) {
      JsonObject response = new JsonObject();
      response.addProperty(ERROR_CODE_RESPONSE, 5);
      response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Subscription not found");
      return response;
    }

    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex  = ParameterParser.getLastIndex(req);

    if(lastIndex < firstIndex) {
      throw new IllegalArgumentException("lastIndex must be greater or equal to firstIndex");
    }

    JsonArray transactions = new JsonArray();
    Blockchain blockchain = Signum.getBlockchain();
    CollectionWithIndex<Transaction> accountTransactions = new CollectionWithIndex<Transaction>(blockchain.getTransactions(tx.getSenderId(), TransactionType.TYPE_ADVANCED_PAYMENT.getType(),
            TransactionType.SUBTYPE_ADVANCED_PAYMENT_SUBSCRIPTION_PAYMENT, TransactionType.SUBTYPE_ADVANCED_PAYMENT_SUBSCRIPTION_PAYMENT, firstIndex, lastIndex), firstIndex, lastIndex);
    for (Transaction transaction : accountTransactions) {
      Attachment.AdvancedPaymentSubscriptionPayment attachment = (AdvancedPaymentSubscriptionPayment) transaction.getAttachment();
      if(attachment.getSubscriptionId() != subscriptionId) {
        continue;
      }
      transactions.add(JSONData.transaction(transaction, blockchain.getHeight()));
    }

    JsonObject response = new JsonObject();
    response.add(TRANSACTIONS_RESPONSE, transactions);

    if(accountTransactions.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, accountTransactions.nextIndex());
    }

    return response;
  }
}
