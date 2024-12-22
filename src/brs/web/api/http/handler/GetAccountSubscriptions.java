package brs.web.api.http.handler;

import brs.Account;
import brs.Alias;
import brs.Signum;
import brs.SignumException;
import brs.Subscription;
import brs.Transaction;
import brs.services.AliasService;
import brs.services.ParameterService;
import brs.services.SubscriptionService;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static brs.web.api.http.common.Parameters.ACCOUNT_PARAMETER;
import static brs.web.api.http.common.Parameters.SUBSCRIPTIONS_RESPONSE;

public final class GetAccountSubscriptions extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final SubscriptionService subscriptionService;
  private final AliasService aliasService;

  public GetAccountSubscriptions(ParameterService parameterService, SubscriptionService subscriptionService, AliasService aliasService) {
    super(new LegacyDocTag[]{LegacyDocTag.ACCOUNTS}, ACCOUNT_PARAMETER);
    this.parameterService = parameterService;
    this.subscriptionService = subscriptionService;
    this.aliasService = aliasService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {

    Account account = parameterService.getAccount(req);

    JsonObject response = new JsonObject();

    JsonArray subscriptions = new JsonArray();

    Collection<Subscription> accountSubscriptions = subscriptionService.getSubscriptionsByParticipant(account.getId());

    for (Subscription accountSubscription : accountSubscriptions) {
      Alias alias = aliasService.getAlias(accountSubscription.getRecipientId());
      Alias tld = alias == null ? null : aliasService.getTLD(alias.getTld());

      Transaction transaction = Signum.getBlockchain().getTransaction(alias == null? accountSubscription.getId() : alias.getId());
      subscriptions.add(JSONData.subscription(accountSubscription, alias, tld, transaction));
    }

    response.add(SUBSCRIPTIONS_RESPONSE, subscriptions);
    return response;
  }
}
