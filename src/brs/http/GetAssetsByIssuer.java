package brs.http;

import brs.Account;
import brs.assetexchange.AssetExchange;
import brs.services.ParameterService;
import brs.util.Convert;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static brs.http.common.Parameters.*;
import static brs.http.common.ResultFields.ASSETS_RESPONSE;

public final class GetAssetsByIssuer extends AbstractAssetsRetrieval {

  private final ParameterService parameterService;
  private final AssetExchange assetExchange;

  GetAssetsByIssuer(ParameterService parameterService, AssetExchange assetExchange) {
    super(new APITag[] {APITag.AE, APITag.ACCOUNTS}, assetExchange, ACCOUNT_PARAMETER, ACCOUNT_PARAMETER, ACCOUNT_PARAMETER, QUANTITY_MININUM_QNT_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.parameterService = parameterService;
    this.assetExchange = assetExchange;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws ParameterException {
    List<Account> accounts = parameterService.getAccounts(req);
    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);
    long minimumQuantity = Convert.parseUnsignedLong(req.getParameter(QUANTITY_MININUM_QNT_PARAMETER));

    JsonObject response = new JsonObject();
    JsonArray accountsJsonArray = new JsonArray();
    response.add(ASSETS_RESPONSE, accountsJsonArray);
    for (Account account : accounts) {
      accountsJsonArray.add(assetsToJson(assetExchange.getAssetsIssuedBy(account.getId(), firstIndex, lastIndex).iterator(), minimumQuantity));
    }
    return response;
  }

}
