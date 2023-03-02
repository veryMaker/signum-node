package brs.http;

import brs.Alias;
import brs.Burst;
import brs.Subscription;
import brs.Transaction;
import brs.services.AliasService;
import brs.services.SubscriptionService;
import brs.util.Convert;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.SUBSCRIPTION_PARAMETER;
import static brs.http.common.ResultFields.ERROR_CODE_RESPONSE;
import static brs.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE;

final class GetSubscription extends APIServlet.JsonRequestHandler {
	
  private final SubscriptionService subscriptionService;
  private final AliasService aliasService;

  GetSubscription(SubscriptionService subscriptionService, AliasService aliasService) {
    super(new APITag[] {APITag.ACCOUNTS}, SUBSCRIPTION_PARAMETER);
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
    
    Transaction transaction = Burst.getBlockchain().getTransaction(subscriptionId);
		
    return JSONData.subscription(subscription, alias, tld, transaction);
  }
}
