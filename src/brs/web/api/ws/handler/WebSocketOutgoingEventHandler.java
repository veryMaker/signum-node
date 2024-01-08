package brs.web.api.ws.handler;

public interface  WebSocketOutgoingEventHandler<T> {

  void notify(T t);
}
