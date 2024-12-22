package brs.web.api.http.handler;

import brs.Blockchain;
import brs.util.Convert;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.INCORRECT_HEIGHT;
import static brs.web.api.http.common.JSONResponses.MISSING_HEIGHT;
import static brs.web.api.http.common.Parameters.HEIGHT_PARAMETER;

public final class GetBlockId extends ApiServlet.JsonRequestHandler {

  private final Blockchain blockchain;

  public GetBlockId(Blockchain blockchain) {
    super(new LegacyDocTag[] {LegacyDocTag.BLOCKS}, HEIGHT_PARAMETER);
    this.blockchain = blockchain;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {

    int height;
    try {
      String heightValue = Convert.emptyToNull(req.getParameter(HEIGHT_PARAMETER));
      if (heightValue == null) {
        return MISSING_HEIGHT;
      }
      height = Integer.parseInt(heightValue);
    } catch (RuntimeException e) {
      return INCORRECT_HEIGHT;
    }

    try {
      JsonObject response = new JsonObject();
      response.addProperty("block", Convert.toUnsignedLong(blockchain.getBlockIdAtHeight(height)));
      return response;
    } catch (RuntimeException e) {
      return INCORRECT_HEIGHT;
    }

  }

}
