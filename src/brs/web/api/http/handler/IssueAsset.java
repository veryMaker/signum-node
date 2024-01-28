package brs.web.api.http.handler;

import brs.*;
import brs.fluxcapacitor.FluxValues;
import brs.services.ParameterService;
import brs.util.Convert;
import brs.util.TextUtils;
import brs.web.api.http.common.APITransactionManager;
import brs.web.api.http.common.JSONResponses;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.*;
import static brs.web.api.http.common.Parameters.*;

public final class IssueAsset extends CreateTransaction {

  private final ParameterService parameterService;
  private final Blockchain blockchain;

  public IssueAsset(ParameterService parameterService, Blockchain blockchain, APITransactionManager apiTransactionManager) {
    super(new LegacyDocTag[]{LegacyDocTag.AE, LegacyDocTag.CREATE_TRANSACTION}, apiTransactionManager,
        NAME_PARAMETER, DESCRIPTION_PARAMETER, QUANTITY_QNT_PARAMETER, DECIMALS_PARAMETER, MINTABLE_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {

    String name = req.getParameter(NAME_PARAMETER);
    String description = req.getParameter(DESCRIPTION_PARAMETER);
    String decimalsValue = Convert.emptyToNull(req.getParameter(DECIMALS_PARAMETER));
    boolean mintable = "true".equals(req.getParameter(MINTABLE_PARAMETER));
    if(mintable && !Signum.getFluxCapacitor().getValue(FluxValues.SMART_TOKEN)) {
      //only after the fork we are allowed to have a mintable assset
      return JSONResponses.incorrect(MINTABLE_PARAMETER);
    }

    if (name == null) {
      return MISSING_NAME;
    }

    name = name.trim();
    if (name.length() < Constants.MIN_ASSET_NAME_LENGTH || name.length() > Constants.MAX_ASSET_NAME_LENGTH) {
      return INCORRECT_ASSET_NAME_LENGTH;
    }

    if (!TextUtils.isInAlphabet(name)) {
      return INCORRECT_ASSET_NAME;
    }

    if (description != null && description.length() > Constants.MAX_ASSET_DESCRIPTION_LENGTH) {
      return INCORRECT_ASSET_DESCRIPTION;
    }

    byte decimals = 0;
    if (decimalsValue != null) {
      try {
        decimals = Byte.parseByte(decimalsValue);
        if (decimals < 0 || decimals > 8) {
          return INCORRECT_DECIMALS;
        }
      } catch (NumberFormatException e) {
        return INCORRECT_DECIMALS;
      }
    }

    long quantityQNT = ParameterParser.getQuantityQNT(req);
    if(quantityQNT == 0L && !mintable) {
      return incorrect(QUANTITY_QNT_PARAMETER);
    }
    Account account = parameterService.getSenderAccount(req);
    Attachment attachment = new Attachment.ColoredCoinsAssetIssuance(name, description, quantityQNT, decimals, blockchain.getHeight(), mintable);
    return createTransaction(req, account, attachment);

  }

}
