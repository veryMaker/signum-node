package brs.web.api.ws.emitter;

import brs.Transaction;
import brs.web.api.ws.WebSocketConnection;
import brs.web.api.ws.common.JSONWebSocketResponse;
import brs.web.api.ws.common.WebsocketEventNames;

import java.util.List;

public class PendingTransactionsAddedEventEmitter extends AbstractWebSocketEventEmitterImpl<List<? extends Transaction>> {

  private static int MAX_TRANSACTIONS = 100;
  public PendingTransactionsAddedEventEmitter(WebSocketConnection connection) {
    super(connection);
  }

  @Override
  public void emit(List<? extends Transaction> transactions) {
    JSONWebSocketResponse<PendingTransactionsAddedEventEmitter.PendingTransactionsAddedPayload> response =
      new JSONWebSocketResponse<>(
        WebsocketEventNames.PENDING_TRANSACTIONS_ADDED,
              new PendingTransactionsAddedPayload(transactions)
      );
    this.getConnection().sendMessage(response.toString());
  }

  private static class PendingTransactionsAddedPayload {
    private final String[] transactionIds;
    private final boolean hasMore;

    public PendingTransactionsAddedPayload(List<? extends Transaction> transactions) {
      this.transactionIds = transactions.stream().map(Transaction::getStringId).limit(MAX_TRANSACTIONS).toArray(String[]::new);
      this.hasMore = transactions.size() > MAX_TRANSACTIONS;
    }
  }
}
