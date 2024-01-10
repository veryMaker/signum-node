package brs.web.api.ws.emitter;

import brs.web.api.ws.WebSocketConnection;

public abstract class AbstractWebSocketEventEmitterImpl<T> implements WebSocketEventEmitter<T> {

  private final WebSocketConnection connection;

  protected AbstractWebSocketEventEmitterImpl(WebSocketConnection connection) {
    this.connection = connection;
  }

  public abstract void emit(T t);

  @Override
  public void emit() {
    // do nothing - optional implementation
  }

  public WebSocketConnection getConnection() {
    return connection;
  }
}
