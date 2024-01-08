package brs.web.api.ws.handler;

import brs.Block;
import brs.web.api.ws.WebSocketConnection;
import brs.web.api.ws.common.JSONWebSocketResponse;

public class BlockPushedEventHandler extends AbstractWebSocketOutgoingEventHandlerImpl<Block> {
  private final int currentHeight;

  public BlockPushedEventHandler(WebSocketConnection connection, int currentHeight) {
      super(connection);
    this.currentHeight = currentHeight;
  }

  @Override
  public void notify(Block block) {
    JSONWebSocketResponse<PushedBlockPayload> response = new JSONWebSocketResponse<>("BlockPushed", new PushedBlockPayload(block, currentHeight));
    this.getConnection().sendMessage(response.toString());
  }

  private class PushedBlockPayload {
    private final String blockId;
    private final int height;
    private final int timestamp;
    private final float progress;
    private final int blockchainHeight;

    public PushedBlockPayload(Block block, int currentHeight) {
      this.blockId = block.getStringId();
      this.height = block.getHeight();
      this.timestamp = block.getTimestamp();
      this.blockchainHeight = currentHeight;
      this.progress = currentHeight > 0 ? (float) block.getHeight() / (float) currentHeight : 0;
    }
  }
}
