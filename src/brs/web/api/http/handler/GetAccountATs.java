package brs.web.api.http.handler;

import brs.Account;
import brs.SignumException;
import brs.services.ATService;
import brs.services.ParameterService;
import brs.util.CollectionWithIndex;
import brs.util.Convert;

import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.ERROR_INCORRECT_REQUEST;
import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.ATS_RESPONSE;
import static brs.web.api.http.common.ResultFields.NEXT_INDEX_RESPONSE;

public final class GetAccountATs extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final ATService atService;

  public GetAccountATs(ParameterService parameterService, ATService atService) {
    super(new LegacyDocTag[] {LegacyDocTag.AT, LegacyDocTag.ACCOUNTS}, ACCOUNT_PARAMETER, MACHINE_CODE_HASH_ID_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.parameterService = parameterService;
    this.atService = atService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
    Account account = parameterService.getAccount(req);

    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex  = ParameterParser.getLastIndex(req);

    if(lastIndex < firstIndex) {
      throw new IllegalArgumentException("lastIndex must be greater or equal to firstIndex");
    }

    Long codeHashId = null;
    String codeHashIdString = Convert.emptyToNull(req.getParameter(MACHINE_CODE_HASH_ID_PARAMETER));
    if(codeHashIdString != null) {
      try {
        codeHashId = Convert.parseUnsignedLong(codeHashIdString);
      } catch (RuntimeException e) {
        return ERROR_INCORRECT_REQUEST;
      }
    }

    CollectionWithIndex<Long> atIds = atService.getATsIssuedBy(account.getId(), codeHashId, firstIndex, lastIndex);
    JsonArray ats = new JsonArray();
    for(long atId : atIds) {
      ats.add(JSONData.at(atService.getAT(atId)));
    }

    JsonObject response = new JsonObject();
    response.add(ATS_RESPONSE, ats);

    if(atIds.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, atIds.nextIndex());
    }

    return response;
  }
}
