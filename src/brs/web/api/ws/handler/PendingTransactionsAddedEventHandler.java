package brs.web.api.ws.handler;

import brs.Transaction;
import brs.web.api.ws.WebSocketConnection;
import brs.web.api.ws.common.JSONWebSocketResponse;

import java.util.List;

public class PendingTransactionsAddedEventHandler extends AbstractWebSocketOutgoingEventHandlerImpl<List<? extends Transaction>> {

  public PendingTransactionsAddedEventHandler(WebSocketConnection connection) {
    super(connection);
  }

  @Override
  public void notify(List<? extends Transaction> transactions) {
    JSONWebSocketResponse<PendingTransactionsAddedEventHandler.PendingTransactionsAddedPayload> response =
      new JSONWebSocketResponse<>("PendingTransactionsAdded",
        new PendingTransactionsAddedEventHandler.PendingTransactionsAddedPayload(transactions));
    this.getConnection().sendMessage(response.toString());
  }

  private class PendingTransactionsAddedPayload {
    private final int transactionCount;

    public PendingTransactionsAddedPayload(List<? extends Transaction> transactions) {
      this.transactionCount = transactions.size();
    }
  }
}
