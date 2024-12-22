package brs.web.api.http.handler;

import brs.Account;
import brs.SignumException;
import brs.Escrow;
import brs.services.EscrowService;
import brs.services.ParameterService;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static brs.web.api.http.common.Parameters.ACCOUNT_PARAMETER;
import static brs.web.api.http.common.Parameters.ESCROWS_RESPONSE;

public final class GetAccountEscrowTransactions extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;

  private final EscrowService escrowService;

  public GetAccountEscrowTransactions(ParameterService parameterService, EscrowService escrowService) {
    super(new LegacyDocTag[]{LegacyDocTag.ACCOUNTS}, ACCOUNT_PARAMETER);
    this.parameterService = parameterService;
    this.escrowService = escrowService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
    final Account account = parameterService.getAccount(req);

    Collection<Escrow> accountEscrows = escrowService.getEscrowTransactionsByParticipant(account.getId());

    JsonObject response = new JsonObject();

    JsonArray escrows = new JsonArray();

    for (Escrow escrow : accountEscrows) {
      escrows.add(JSONData.escrowTransaction(escrow));
    }

    response.add(ESCROWS_RESPONSE, escrows);
    return response;
  }
}
