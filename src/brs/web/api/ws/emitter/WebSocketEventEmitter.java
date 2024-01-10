package brs.web.api.ws.emitter;

public interface WebSocketEventEmitter<T> {

  void emit(T t);
  void emit();
}
