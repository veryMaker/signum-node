package brs.web.api.http.common;

import brs.BurstException;
import com.google.gson.JsonElement;

public final class ParameterException extends BurstException {

  private transient final JsonElement errorResponse;

  public ParameterException(JsonElement errorResponse) {
    this.errorResponse = errorResponse;
  }

  public JsonElement getErrorResponse() {
    return errorResponse;
  }

}
