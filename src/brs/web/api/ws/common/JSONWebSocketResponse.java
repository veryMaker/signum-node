package brs.web.api.ws.common;

import com.google.gson.Gson;

public class JSONWebSocketResponse<T> extends BaseWebSocketResponse<T> {
  public JSONWebSocketResponse(String eventName, T payload) {
    super(eventName, payload);
  }

  public JSONWebSocketResponse(String eventName) {
    super(eventName, null);
  }

  @Override
  public String toString() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }
}
