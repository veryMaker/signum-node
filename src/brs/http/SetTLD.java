package brs.http;

import brs.*;
import brs.fluxcapacitor.FluxValues;
import brs.services.AliasService;
import brs.services.ParameterService;
import brs.util.Convert;
import brs.util.TextUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.http.JSONResponses.*;
import static brs.http.common.Parameters.TLD_PARAMETER;
import static brs.http.common.Parameters.AMOUNT_NQT_PARAMETER;
import static brs.http.common.ResultFields.ERROR_CODE_RESPONSE;
import static brs.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE;

public final class SetTLD extends CreateTransaction {

  private final ParameterService parameterService;
  private final Blockchain blockchain;
  private final AliasService aliasService;

  public SetTLD(ParameterService parameterService, Blockchain blockchain, AliasService aliasService, APITransactionManager apiTransactionManager) {
    super(new APITag[] {APITag.ALIASES, APITag.CREATE_TRANSACTION}, apiTransactionManager, TLD_PARAMETER, AMOUNT_NQT_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
    this.aliasService = aliasService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    String tldName = Convert.emptyToNull(req.getParameter(TLD_PARAMETER));

    if (tldName == null) {
      return missing(TLD_PARAMETER);
    }

    tldName = tldName.trim();
    if (tldName.isEmpty() || tldName.length() > Constants.MAX_TLD_LENGTH) {
      return incorrect(TLD_PARAMETER);
    }

    if (!TextUtils.isInAlphabet(tldName)) {
      return incorrect(TLD_PARAMETER);
    }

    Alias alias = aliasService.getTLD(tldName);
    if (alias != null) {
      JsonObject response = new JsonObject();
      response.addProperty(ERROR_CODE_RESPONSE, 8);
      response.addProperty(ERROR_DESCRIPTION_RESPONSE, "\"" + tldName + "\" is already used");
      return response;
    }
    
    long recipient = 0L;
    long amountNQT = ParameterParser.getAmountNQT(req);
    if(amountNQT < TransactionType.BASELINE_TLD_ASSIGNMENT_FACTOR * Burst.getFluxCapacitor().getValue(FluxValues.FEE_QUANT, blockchain.getLastBlock().getHeight())) {
      return incorrect(AMOUNT_NQT_PARAMETER);
    }

    Account account = parameterService.getSenderAccount(req);
    Attachment attachment = new Attachment.MessagingTLDAssignment(tldName, blockchain.getHeight());
    return createTransaction(req, account, recipient, amountNQT, attachment);
  }

}
