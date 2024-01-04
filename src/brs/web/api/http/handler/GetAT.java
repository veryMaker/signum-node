package brs.web.api.http.handler;

import brs.Attachment;
import brs.Blockchain;
import brs.BurstException;
import brs.Transaction;
import brs.at.AT;
import brs.at.AtApiHelper;
import brs.at.AtMachineState;
import brs.services.ParameterService;

import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.AT_PARAMETER;
import static brs.web.api.http.common.Parameters.INCLUDE_DETAILS_PARAMETER;

public final class GetAT extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final Blockchain blockchain;

  public GetAT(ParameterService parameterService, Blockchain blockchain) {
    super(new LegacyDocTag[]{LegacyDocTag.AT}, AT_PARAMETER, INCLUDE_DETAILS_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    boolean includeDetails = !("false".equalsIgnoreCase(req.getParameter(INCLUDE_DETAILS_PARAMETER)));

    AT at = parameterService.getAT(req);
    AtMachineState atCreation = null;

    if(includeDetails) {
      Transaction transaction = blockchain.getTransaction(AtApiHelper.getLong(at.getId()));
      if(transaction.getAttachment()!=null && transaction.getAttachment() instanceof Attachment.AutomatedTransactionsCreation) {
        Attachment.AutomatedTransactionsCreation atCreationAttachment = (Attachment.AutomatedTransactionsCreation)transaction.getAttachment();

        atCreation = new AtMachineState(at.getId(), at.getCreator(), atCreationAttachment.getCreationBytes(), at.getCreationBlockHeight());
      }
    }

    return JSONData.at(at, atCreation, includeDetails);
  }

}
