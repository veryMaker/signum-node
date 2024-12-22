package brs.web.api.http.handler;

import brs.*;
import brs.services.ParameterService;
import brs.util.Convert;
import brs.web.api.http.common.APITransactionManager;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.*;
import static brs.web.api.http.common.Parameters.*;

public final class DGSListing extends CreateTransaction {

  private final ParameterService parameterService;
  private final Blockchain blockchain;

  public DGSListing(ParameterService parameterService, Blockchain blockchain, APITransactionManager apiTransactionManager) {
    super(new LegacyDocTag[]{LegacyDocTag.DGS, LegacyDocTag.CREATE_TRANSACTION}, apiTransactionManager, NAME_PARAMETER, DESCRIPTION_PARAMETER, TAGS_PARAMETER, QUANTITY_PARAMETER, PRICE_NQT_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {

    String name = Convert.emptyToNull(req.getParameter(NAME_PARAMETER));
    String description = Convert.nullToEmpty(req.getParameter(DESCRIPTION_PARAMETER));
    String tags = Convert.nullToEmpty(req.getParameter(TAGS_PARAMETER));
    long priceNQT = ParameterParser.getPriceNQT(req);
    int quantity = ParameterParser.getGoodsQuantity(req);

    if (name == null) {
      return MISSING_NAME;
    }
    name = name.trim();
    if (name.length() > Constants.MAX_DGS_LISTING_NAME_LENGTH) {
      return INCORRECT_DGS_LISTING_NAME;
    }

    if (description.length() > Constants.MAX_DGS_LISTING_DESCRIPTION_LENGTH) {
      return INCORRECT_DGS_LISTING_DESCRIPTION;
    }

    if (tags.length() > Constants.MAX_DGS_LISTING_TAGS_LENGTH) {
      return INCORRECT_DGS_LISTING_TAGS;
    }

    Account account = parameterService.getSenderAccount(req);
    Attachment attachment = new Attachment.DigitalGoodsListing(name, description, tags, quantity, priceNQT, blockchain.getHeight());
    return createTransaction(req, account, attachment);

  }

}
