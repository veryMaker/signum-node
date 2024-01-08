package brs.web.api.http.handler;

import brs.Account;
import brs.BurstException;
import brs.services.AccountService;
import brs.services.ParameterService;
import brs.util.Convert;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.ACCOUNTS_RESPONSE;
import static brs.web.api.http.common.Parameters.ACCOUNT_PARAMETER;

public final class GetAccountsWithRewardRecipient extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final AccountService accountService;

  public GetAccountsWithRewardRecipient(ParameterService parameterService, AccountService accountService) {
    super(new LegacyDocTag[] {LegacyDocTag.ACCOUNTS, LegacyDocTag.MINING, LegacyDocTag.INFO}, ACCOUNT_PARAMETER);
    this.parameterService = parameterService;
    this.accountService = accountService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    JsonObject response = new JsonObject();

    Account targetAccount = parameterService.getAccount(req);

    JsonArray accounts = new JsonArray();

    for (Account.RewardRecipientAssignment assignment : accountService.getAccountsWithRewardRecipient(targetAccount.getId())) {
      accounts.add(Convert.toUnsignedLong(assignment.getAccountId()));
    }

    if(accountService.getRewardRecipientAssignment(targetAccount) == null) {
      accounts.add(Convert.toUnsignedLong(targetAccount.getId()));
    }

    response.add(ACCOUNTS_RESPONSE, accounts);

    return response;
  }
}
