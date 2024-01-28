package brs.web.api.http.handler;

import brs.*;
import brs.fluxcapacitor.FluxValues;
import brs.services.AliasService;
import brs.services.ParameterService;
import brs.util.Convert;
import brs.util.TextUtils;
import brs.web.api.http.common.APITransactionManager;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.*;
import static brs.web.api.http.common.Parameters.ALIAS_NAME_PARAMETER;
import static brs.web.api.http.common.Parameters.ALIAS_URI_PARAMETER;
import static brs.web.api.http.common.Parameters.TLD_PARAMETER;
import static brs.web.api.http.common.ResultFields.ERROR_CODE_RESPONSE;
import static brs.web.api.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE;

public final class SetAlias extends CreateTransaction {

  private final ParameterService parameterService;
  private final Blockchain blockchain;
  private final AliasService aliasService;

  public SetAlias(ParameterService parameterService, Blockchain blockchain, AliasService aliasService, APITransactionManager apiTransactionManager) {
    super(new LegacyDocTag[] {LegacyDocTag.ALIASES, LegacyDocTag.CREATE_TRANSACTION}, apiTransactionManager, ALIAS_NAME_PARAMETER, ALIAS_URI_PARAMETER, TLD_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
    this.aliasService = aliasService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    String aliasName = Convert.emptyToNull(req.getParameter(ALIAS_NAME_PARAMETER));
    String aliasURI = Convert.nullToEmpty(req.getParameter(ALIAS_URI_PARAMETER));
    String tldName = Convert.emptyToNull(req.getParameter(TLD_PARAMETER));
    long tld = 0L;

    if (aliasName == null) {
      return MISSING_ALIAS_NAME;
    }

    aliasName = aliasName.trim();
    if (aliasName.isEmpty() || aliasName.length() > Constants.MAX_ALIAS_LENGTH) {
      return INCORRECT_ALIAS_LENGTH;
    }

    if (Signum.getFluxCapacitor().getValue(FluxValues.SMART_ALIASES)) {
      if (!TextUtils.isInAlphabetOrUnderline(aliasName)) {
        return INCORRECT_ALIAS_NAME;
      }
    }
    else{
      if (!TextUtils.isInAlphabet(aliasName)) {
        return INCORRECT_ALIAS_NAME;
      }
    }

    aliasURI = aliasURI.trim();
    if (aliasURI.length() > Constants.MAX_ALIAS_URI_LENGTH) {
      return INCORRECT_URI_LENGTH;
    }

    if(tldName != null) {
      Alias aliasTLD = aliasService.getTLD(tldName);
      if(aliasTLD == null) {
        return incorrect(TLD_PARAMETER);
      }
      tld = aliasTLD.getId();
    }

    Account account = parameterService.getSenderAccount(req);

    Alias alias = aliasService.getAlias(aliasName, tld);
    if (alias != null && alias.getAccountId() != account.getId()) {
      JsonObject response = new JsonObject();
      response.addProperty(ERROR_CODE_RESPONSE, 8);
      response.addProperty(ERROR_DESCRIPTION_RESPONSE, "\"" + aliasName + "\" is already used");
      return response;
    }

    Attachment attachment = new Attachment.MessagingAliasAssignment(aliasName, aliasURI, tld, blockchain.getHeight());
    return createTransaction(req, account, attachment);

  }

}
