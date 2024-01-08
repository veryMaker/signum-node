package brs.web.api.ws.handler;

import brs.Block;
import brs.Transaction;
import brs.web.api.ws.WebSocketConnection;
import brs.web.api.ws.common.JSONWebSocketResponse;

public class BlockGeneratedEventHandler extends AbstractWebSocketOutgoingEventHandlerImpl<Block> {
  public BlockGeneratedEventHandler(WebSocketConnection connection) {
      super(connection);
  }

  @Override
  public void notify(Block block) {
    JSONWebSocketResponse<GeneratedBlockPayload> response = new JSONWebSocketResponse<>("BlockGenerated", new GeneratedBlockPayload(block));
    this.getConnection().sendMessage(response.toString());
  }

  private class GeneratedBlockPayload {
    private final String blockId;
    private final int height;
    private final int timestamp;
    private final String[] transactionIds;

    public GeneratedBlockPayload(Block block) {
      this.blockId = block.getStringId();
      this.height = block.getHeight();
      this.timestamp = block.getTimestamp();
      this.transactionIds = block.getTransactions().stream().map(Transaction::getStringId).toArray(String[]::new);
    }
  }
}
