package brs.web.api.ws.handler;

import brs.web.api.ws.WebSocketConnection;

public abstract class AbstractWebSocketOutgoingEventHandlerImpl<T> implements WebSocketOutgoingEventHandler<T> {

  private final WebSocketConnection connection;

  protected AbstractWebSocketOutgoingEventHandlerImpl(WebSocketConnection connection) {
    this.connection = connection;
  }

  @Override
  public abstract void notify(T t);

  public WebSocketConnection getConnection() {
    return connection;
  }
}
