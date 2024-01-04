package brs.web.api.http.handler;

import brs.Blockchain;
import brs.BurstException;
import brs.services.ParameterService;
import brs.util.Convert;

import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.*;

public final class GetTransactionIds extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final Blockchain blockchain;

  public GetTransactionIds(ParameterService parameterService, Blockchain blockchain) {
    super(new LegacyDocTag[]{LegacyDocTag.ACCOUNTS}, RECIPIENT_PARAMETER, SENDER_PARAMETER,
        TIMESTAMP_PARAMETER, TYPE_PARAMETER, SUBTYPE_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER,
        NUMBER_OF_CONFIRMATIONS_PARAMETER, INCLUDE_INDIRECT_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
  }

  @Override
  public JsonElement processRequest(HttpServletRequest req) throws BurstException {

    Long senderId = null, recipientId = null;
    String senderParameter = Convert.emptyToNull(req.getParameter(SENDER_PARAMETER));
    if(senderParameter != null) {
      senderId = Convert.parseUnsignedLong(senderParameter);
    }
    String recipientParameter = Convert.emptyToNull(req.getParameter(RECIPIENT_PARAMETER));
    if(recipientParameter != null) {
      recipientId = Convert.parseUnsignedLong(recipientParameter);
    }

    int timestamp = ParameterParser.getTimestamp(req);
    int numberOfConfirmations = parameterService.getNumberOfConfirmations(req);

    byte type;
    byte subtype;
    try {
      type = Byte.parseByte(req.getParameter(TYPE_PARAMETER));
    } catch (NumberFormatException e) {
      type = -1;
    }
    try {
      subtype = Byte.parseByte(req.getParameter(SUBTYPE_PARAMETER));
    } catch (NumberFormatException e) {
      subtype = -1;
    }

    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    JsonArray transactionIds = new JsonArray();
    for (Long transactionId : blockchain.getTransactionIds(senderId, recipientId, numberOfConfirmations,
        type, subtype, timestamp, firstIndex, lastIndex, parameterService.getIncludeIndirect(req))) {
      transactionIds.add(Convert.toUnsignedLong(transactionId));
    }

    JsonObject response = new JsonObject();
    response.add("transactionIds", transactionIds);
    return response;
  }

}
