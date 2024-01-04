package brs.web.api.http.handler;

import brs.Account;
import brs.Burst;
import brs.BurstException;
import brs.Subscription;
import brs.Transaction;
import brs.services.ParameterService;
import brs.services.SubscriptionService;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.ACCOUNT_PARAMETER;

public final class GetSubscriptionsToAccount extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final SubscriptionService subscriptionService;

  public GetSubscriptionsToAccount(ParameterService parameterService, SubscriptionService subscriptionService) {
    super(new LegacyDocTag[] {LegacyDocTag.ACCOUNTS}, ACCOUNT_PARAMETER);
    this.parameterService = parameterService;
    this.subscriptionService = subscriptionService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    final Account account = parameterService.getAccount(req);

    JsonObject response = new JsonObject();

    JsonArray subscriptions = new JsonArray();

    for (Subscription subscription : subscriptionService.getSubscriptionsToId(account.getId())) {

      Transaction transaction = Burst.getBlockchain().getTransaction(subscription.getId());
      subscriptions.add(JSONData.subscription(subscription, null, null, transaction));
    }

    response.add("subscriptions", subscriptions);
    return response;
  }
}
