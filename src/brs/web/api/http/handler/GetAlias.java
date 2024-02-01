package brs.web.api.http.handler;

import brs.Alias;
import brs.Alias.Offer;
import brs.services.AliasService;
import brs.services.ParameterService;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterException;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.*;

public final class GetAlias extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final AliasService aliasService;

  public GetAlias(ParameterService parameterService, AliasService aliasService) {
    super(new LegacyDocTag[] {LegacyDocTag.ALIASES}, ALIAS_PARAMETER, ALIAS_NAME_PARAMETER, TLD_PARAMETER);
    this.parameterService = parameterService;
    this.aliasService = aliasService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws ParameterException {
    final Alias alias = parameterService.getAlias(req);
    final Offer offer = aliasService.getOffer(alias);
    final Alias tld = aliasService.getTLD(alias.getTLD());

    return JSONData.alias(alias, tld, offer, 0);
  }

}
