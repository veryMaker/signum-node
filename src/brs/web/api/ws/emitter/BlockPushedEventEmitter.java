package brs.web.api.ws.emitter;

import brs.Block;
import brs.web.api.ws.WebSocketConnection;
import brs.web.api.ws.common.JSONWebSocketResponse;
import brs.web.api.ws.common.WebsocketEventNames;

public class BlockPushedEventEmitter extends AbstractWebSocketEventEmitterImpl<Block> {
  private final int currentHeight;

  public BlockPushedEventEmitter(WebSocketConnection connection, int currentHeight) {
    super(connection);
    this.currentHeight = currentHeight;
  }

  @Override
  public void emit(Block block) {
    JSONWebSocketResponse<PushedBlockPayload> response = new JSONWebSocketResponse<>(
      WebsocketEventNames.BLOCK_PUSHED,
      new PushedBlockPayload(block, currentHeight)
    );
    this.getConnection().sendMessage(response.toString());
  }

  private class PushedBlockPayload {
    private final int timestamp;
    private final String blockId;
    private final int localHeight;
    private final int globalHeight;
    private final float progress;

    public PushedBlockPayload(Block block, int globalHeight) {
      this.blockId = block.getStringId();
      this.localHeight = block.getHeight();
      this.timestamp = block.getTimestamp();
      this.globalHeight = globalHeight;
      this.progress = globalHeight > 0 ? (float) block.getHeight() / (float) globalHeight : 0;
    }
  }
}
