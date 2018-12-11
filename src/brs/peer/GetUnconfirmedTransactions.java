package brs.peer;

import static brs.http.common.ResultFields.UNCONFIRMED_TRANSACTIONS_RESPONSE;

import brs.Transaction;
import brs.TransactionProcessor;
import brs.peer.PeerServlet.ExtendedProcessRequest;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class GetUnconfirmedTransactions extends PeerServlet.ExtendedPeerRequestHandler {

  private final TransactionProcessor transactionProcessor;

  private final Logger logger = LoggerFactory.getLogger(GetUnconfirmedTransactions.class);

  GetUnconfirmedTransactions(TransactionProcessor transactionProcessor) {
    this.transactionProcessor = transactionProcessor;
  }

  @Override
  ExtendedProcessRequest extendedProcessRequest(JSONObject request, Peer peer) {
    JSONObject response = new JSONObject();

    final List<Transaction> unconfirmedTransactions = transactionProcessor.getAllUnconfirmedTransactionsFor(peer);

    JSONArray transactionsData = new JSONArray();
    for (Transaction transaction : unconfirmedTransactions) {
      transactionsData.add(transaction.getJSONObject());
    }

    response.put(UNCONFIRMED_TRANSACTIONS_RESPONSE, transactionsData);

    return new ExtendedProcessRequest(response, () -> transactionProcessor.markFingerPrintsOf(peer, unconfirmedTransactions));
  }

}
