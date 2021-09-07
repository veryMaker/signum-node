package brs.http;

import brs.Account;
import brs.BurstException;
import brs.services.ATService;
import brs.services.ParameterService;
import brs.util.Convert;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static brs.http.JSONResponses.ERROR_INCORRECT_REQUEST;
import static brs.http.common.Parameters.ACCOUNT_PARAMETER;
import static brs.http.common.Parameters.MACHINE_CODE_HASH_ID_PARAMETER;
import static brs.http.common.ResultFields.ATS_RESPONSE;

public final class GetAccountATs extends APIServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final ATService atService;
  
  GetAccountATs(ParameterService parameterService, ATService atService) {
    super(new APITag[] {APITag.AT, APITag.ACCOUNTS}, ACCOUNT_PARAMETER, MACHINE_CODE_HASH_ID_PARAMETER);
    this.parameterService = parameterService;
    this.atService = atService;
  }
	
  @Override
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    Account account = parameterService.getAccount(req); // TODO this is super redundant
    
    Long codeHashId = null;
    String codeHashIdString = Convert.emptyToNull(req.getParameter(MACHINE_CODE_HASH_ID_PARAMETER));
    if(codeHashIdString != null) {
      try {
        codeHashId = Convert.parseUnsignedLong(codeHashIdString);
      } catch (RuntimeException e) {
        return ERROR_INCORRECT_REQUEST;
      }
    }
		
    List<Long> atIds = atService.getATsIssuedBy(account.getId(), codeHashId);
    JsonArray ats = new JsonArray();
    for(long atId : atIds) {
      ats.add(JSONData.at(atService.getAT(atId)));
    }
		
    JsonObject response = new JsonObject();
    response.add(ATS_RESPONSE, ats);
    return response;
  }
}
