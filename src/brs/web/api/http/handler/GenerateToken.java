package brs.web.api.http.handler;

import brs.Token;
import brs.services.TimeService;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.Constants.TOKEN;
import static brs.Constants.WEBSITE;
import static brs.web.api.http.common.JSONResponses.*;
import static brs.web.api.http.common.Parameters.SECRET_PHRASE_PARAMETER;

public final class GenerateToken extends ApiServlet.JsonRequestHandler {

  private final TimeService timeService;

  public GenerateToken(TimeService timeService) {
    super(new LegacyDocTag[] {LegacyDocTag.TOKENS}, WEBSITE, SECRET_PHRASE_PARAMETER);
    this.timeService = timeService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {

    String secretPhrase = req.getParameter(SECRET_PHRASE_PARAMETER);
    String website = req.getParameter(WEBSITE);
    if (secretPhrase == null) {
      return MISSING_SECRET_PHRASE;
    } else if (website == null) {
      return MISSING_WEBSITE;
    }

    try {

      String tokenString = Token.generateToken(secretPhrase, website.trim(), timeService.getEpochTime());

      JsonObject response = new JsonObject();
      response.addProperty(TOKEN, tokenString);

      return response;

    } catch (RuntimeException e) {
      return INCORRECT_WEBSITE;
    }

  }

//  @Override
//  boolean requirePost() {
//    return true;
//  }

}
