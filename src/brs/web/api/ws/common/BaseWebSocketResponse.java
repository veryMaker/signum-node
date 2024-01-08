package brs.web.api.ws.common;

public abstract class BaseWebSocketResponse<T> {

  private final String eventName;
  private final T payload;

  public BaseWebSocketResponse(String eventName, T payload) {
    this.eventName = eventName;
    this.payload = payload;
  }

  public abstract String toString();

  public T getPayload() {
    return payload;
  }

  public String getEventName() {
    return eventName;
  }
}
