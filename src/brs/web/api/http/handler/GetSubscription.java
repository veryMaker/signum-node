package brs.web.api.http.handler;

import brs.Alias;
import brs.Signum;
import brs.Subscription;
import brs.Transaction;
import brs.services.AliasService;
import brs.services.SubscriptionService;
import brs.util.Convert;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.SUBSCRIPTION_PARAMETER;
import static brs.web.api.http.common.ResultFields.ERROR_CODE_RESPONSE;
import static brs.web.api.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE;

public final class GetSubscription extends ApiServlet.JsonRequestHandler {

  private final SubscriptionService subscriptionService;
  private final AliasService aliasService;

  public GetSubscription(SubscriptionService subscriptionService, AliasService aliasService) {
    super(new LegacyDocTag[] {LegacyDocTag.ACCOUNTS}, SUBSCRIPTION_PARAMETER);
    this.subscriptionService = subscriptionService;
    this.aliasService = aliasService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {
    long subscriptionId;
    try {
      subscriptionId = Convert.parseUnsignedLong(Convert.emptyToNull(req.getParameter(SUBSCRIPTION_PARAMETER)));
    }
    catch(Exception e) {
      JsonObject response = new JsonObject();
      response.addProperty(ERROR_CODE_RESPONSE, 3);
      response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid or not specified subscription");
      return response;
    }

    Subscription subscription = subscriptionService.getSubscription(subscriptionId);

    if(subscription == null) {
      JsonObject response = new JsonObject();
      response.addProperty(ERROR_CODE_RESPONSE, 5);
      response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Subscription not found");
      return response;
    }
    Alias alias = aliasService.getAlias(subscription.getRecipientId());
    Alias tld = alias == null ? null : aliasService.getTLD(alias.getTLD());

    Transaction transaction = Signum.getBlockchain().getTransaction(subscriptionId);

    return JSONData.subscription(subscription, alias, tld, transaction);
  }
}
