package brs.web.api.http.common;

import brs.SignumException;
import com.google.gson.JsonElement;

public final class ParameterException extends SignumException {

  private transient final JsonElement errorResponse;

  public ParameterException(JsonElement errorResponse) {
    this.errorResponse = errorResponse;
  }

  public JsonElement getErrorResponse() {
    return errorResponse;
  }

}
