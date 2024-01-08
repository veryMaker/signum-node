package brs.web.api.ws.handler;

import brs.web.api.ws.WebSocketConnection;
import brs.web.api.ws.common.JSONWebSocketResponse;

public class TestEventHandler extends AbstractWebSocketOutgoingEventHandlerImpl<String> {
  public TestEventHandler(WebSocketConnection connection) {
      super(connection);
  }

  @Override
  public void notify(String data) {
    JSONWebSocketResponse<TestPayload> response = new JSONWebSocketResponse<>("TestEvent", new TestPayload(data));
    this.getConnection().sendMessage(response.toString());
  }

  private class TestPayload {
    public TestPayload(String data) {
    }
  }
}
