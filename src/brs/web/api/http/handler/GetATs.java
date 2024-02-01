package brs.web.api.http.handler;

import brs.Attachment;
import brs.Blockchain;
import brs.Signum;
import brs.SignumException;
import brs.Transaction;
import brs.at.AT;
import brs.at.AtApiHelper;
import brs.at.AtMachineState;
import brs.services.ATService;
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

public final class GetATs extends ApiServlet.JsonRequestHandler {

  private final ATService atService;

  public GetATs(ATService atService) {
    super(new LegacyDocTag[] {LegacyDocTag.AT, LegacyDocTag.ACCOUNTS}, MACHINE_CODE_HASH_ID_PARAMETER, INCLUDE_DETAILS_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.atService = atService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {

    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex  = ParameterParser.getLastIndex(req);

    if(lastIndex < firstIndex) {
      throw new IllegalArgumentException("lastIndex must be greater or equal to firstIndex");
    }

    boolean includeDetails = !("false".equalsIgnoreCase(req.getParameter(INCLUDE_DETAILS_PARAMETER)));

    Long codeHashId = null;
    String codeHashIdString = Convert.emptyToNull(req.getParameter(MACHINE_CODE_HASH_ID_PARAMETER));
    if(codeHashIdString != null) {
      try {
        codeHashId = Convert.parseUnsignedLong(codeHashIdString);
      } catch (RuntimeException e) {
        return ERROR_INCORRECT_REQUEST;
      }
    }

    Blockchain blockchain = Signum.getBlockchain();
    CollectionWithIndex<Long> atIds = atService.getATsIssuedBy(null, codeHashId, firstIndex, lastIndex);
    JsonArray ats = new JsonArray();
    for(long atId : atIds) {
      AtMachineState atCreation = null;
      AT at = atService.getAT(atId);

      if(includeDetails) {
        Transaction transaction = blockchain.getTransaction(AtApiHelper.getLong(at.getId()));
        if(transaction.getAttachment()!=null && transaction.getAttachment() instanceof Attachment.AutomatedTransactionsCreation) {
          Attachment.AutomatedTransactionsCreation atCreationAttachment = (Attachment.AutomatedTransactionsCreation)transaction.getAttachment();

          atCreation = new AtMachineState(at.getId(), at.getCreator(), atCreationAttachment.getCreationBytes(), at.getCreationBlockHeight());
        }
      }

      ats.add(JSONData.at(at, atCreation, includeDetails));
    }

    JsonObject response = new JsonObject();
    response.add(ATS_RESPONSE, ats);

    if(atIds.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, atIds.nextIndex());
    }

    return response;
  }
}
