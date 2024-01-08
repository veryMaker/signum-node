package brs.web.api.ws.handler;

import brs.Block;
import brs.web.api.ws.WebSocketConnection;
import brs.web.api.ws.common.ConnectedEventData;
import brs.web.api.ws.common.JSONWebSocketResponse;

public class ConnectedEventHandler extends AbstractWebSocketOutgoingEventHandlerImpl<ConnectedEventData> {

  public ConnectedEventHandler(WebSocketConnection connection) {
      super(connection);
  }

  @Override
  public void notify(ConnectedEventData data) {
    JSONWebSocketResponse<ConnectedPayload> response = new JSONWebSocketResponse<>("Connected", new ConnectedPayload(data));
    this.getConnection().sendMessage(response.toString());
  }

//  public static class ConnectionData {
//    String version;
//    int blockchainHeight;
//    int nodeHeight;
//    String networkName;
//    String message;
//  }

  private class ConnectedPayload {

    private final String version;
    private final int blockchainHeight;
    private final int nodeHeight;
    private final String networkName;
    private final String message;

    public ConnectedPayload(ConnectedEventData data) {
      this.version = data.version;
      this.blockchainHeight = data.blockchainHeight;
      this.nodeHeight = data.nodeHeight;
      this.networkName = data.networkName;
      this.message = data.message;
    }
  }
}
