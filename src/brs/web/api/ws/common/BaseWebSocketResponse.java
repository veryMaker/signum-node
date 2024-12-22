package brs.web.api.ws.common;

import com.google.gson.annotations.SerializedName;

public abstract class BaseWebSocketResponse<T> {

  @SerializedName("e")
  private final String eventName;
  @SerializedName("p")
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
