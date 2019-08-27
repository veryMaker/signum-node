package brs.http;

import brs.*;
import brs.props.Props;
import brs.services.*;
import brs.util.JSON;
import brs.util.Subnet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.util.*;

import static brs.http.JSONResponses.*;

public final class APIServlet extends HttpServlet {

  private static final Logger logger = LoggerFactory.getLogger(APIServlet.class);

  private final Set<Subnet> allowedBotHosts;
  private final boolean acceptSurplusParams;

  public APIServlet(DependencyProvider dp, Set<Subnet> allowedBotHosts) { // TODO each one should just take dp
    enforcePost = dp.propertyService.get(Props.API_SERVER_ENFORCE_POST);
    allowedOrigins = dp.propertyService.get(Props.API_ALLOWED_ORIGINS);
    this.allowedBotHosts = allowedBotHosts;
    this.acceptSurplusParams = dp.propertyService.get(Props.API_ACCEPT_SURPLUS_PARAMS);

    final Map<String, HttpRequestHandler> map = new HashMap<>();

    map.put("broadcastTransaction", new BroadcastTransaction(dp.transactionProcessor, dp.parameterService, dp.transactionService));
    map.put("calculateFullHash", new CalculateFullHash());
    map.put("cancelAskOrder", new CancelAskOrder(dp));
    map.put("cancelBidOrder", new CancelBidOrder(dp));
    map.put("decryptFrom", new DecryptFrom(dp.parameterService));
    map.put("dgsListing", new DGSListing(dp));
    map.put("dgsDelisting", new DGSDelisting(dp));
    map.put("dgsDelivery", new DGSDelivery(dp));
    map.put("dgsFeedback", new DGSFeedback(dp));
    map.put("dgsPriceChange", new DGSPriceChange(dp));
    map.put("dgsPurchase", new DGSPurchase(dp));
    map.put("dgsQuantityChange", new DGSQuantityChange(dp));
    map.put("dgsRefund", new DGSRefund(dp));
    map.put("encryptTo", new EncryptTo(dp.parameterService, dp.accountService));
    map.put("generateToken", new GenerateToken(dp.timeService));
    map.put("getAccount", new GetAccount(dp.parameterService, dp.accountService));
    map.put("getAccountsWithName", new GetAccountsWithName(dp.accountService));
    map.put("getAccountBlockIds", new GetAccountBlockIds(dp.parameterService, dp.blockchain));
    map.put("getAccountBlocks", new GetAccountBlocks(dp.blockchain, dp.parameterService, dp.blockService));
    map.put("getAccountId", new GetAccountId());
    map.put("getAccountPublicKey", new GetAccountPublicKey(dp.parameterService));
    map.put("getAccountTransactionIds", new GetAccountTransactionIds(dp.parameterService, dp.blockchain));
    map.put("getAccountTransactions", new GetAccountTransactions(dp.parameterService, dp.blockchain));
    map.put("getAccountLessors", new GetAccountLessors(dp.parameterService, dp.blockchain));
    map.put("sellAlias", new SellAlias(dp));
    map.put("buyAlias", new BuyAlias(dp));
    map.put("getAlias", new GetAlias(dp.parameterService, dp.aliasService));
    map.put("getAliases", new GetAliases(dp.parameterService, dp.aliasService));
    map.put("getAllAssets", new GetAllAssets(dp.assetExchange));
    map.put("getAsset", new GetAsset(dp.parameterService, dp.assetExchange));
    map.put("getAssets", new GetAssets(dp.assetExchange));
    map.put("getAssetIds", new GetAssetIds(dp.assetExchange));
    map.put("getAssetsByIssuer", new GetAssetsByIssuer(dp.parameterService, dp.assetExchange));
    map.put("getAssetAccounts", new GetAssetAccounts(dp.parameterService, dp.assetExchange));
    map.put("getBalance", new GetBalance(dp.parameterService));
    map.put("getBlock", new GetBlock(dp.blockchain, dp.blockService));
    map.put("getBlockId", new GetBlockId(dp.blockchain));
    map.put("getBlocks", new GetBlocks(dp.blockchain, dp.blockService));
    map.put("getdp.blockchainStatus", new GetBlockchainStatus(dp.blockchainProcessor, dp.blockchain, dp.timeService));
    map.put("getConstants", new GetConstants(dp));
    map.put("getDGSGoods", new GetDGSGoods(dp.digitalGoodsStoreService));
    map.put("getDGSGood", new GetDGSGood(dp.parameterService));
    map.put("getDGSPurchases", new GetDGSPurchases(dp.digitalGoodsStoreService));
    map.put("getDGSPurchase", new GetDGSPurchase(dp.parameterService));
    map.put("getDGSPendingPurchases", new GetDGSPendingPurchases(dp.digitalGoodsStoreService));
    map.put("getECBlock", new GetECBlock(dp.blockchain, dp.timeService, dp.economicClustering));
    map.put("getMyInfo", GetMyInfo.Companion.getInstance());
    map.put("getPeer", GetPeer.Companion.getInstance());
    map.put("getMyPeerInfo", new GetMyPeerInfo(dp.transactionProcessor));
    map.put("getPeers", GetPeers.Companion.getInstance());
    map.put("getState", new GetState(dp.blockchain, dp.blockchainProcessor, dp.assetExchange, dp.accountService, dp.escrowService, dp.aliasService, dp.timeService, dp.generator, dp.propertyService));
    map.put("getTime", new GetTime(dp.timeService));
    map.put("getTrades", new GetTrades(dp.parameterService, dp.assetExchange));
    map.put("getAllTrades", new GetAllTrades(dp.assetExchange));
    map.put("getAssetTransfers", new GetAssetTransfers(dp.parameterService, dp.accountService, dp.assetExchange));
    map.put("getTransaction", new GetTransaction(dp.transactionProcessor, dp.blockchain));
    map.put("getTransactionBytes", new GetTransactionBytes(dp.blockchain, dp.transactionProcessor));
    map.put("getUnconfirmedTransactionIds", new GetUnconfirmedTransactionIds(dp.transactionProcessor, dp.indirectIncomingService, dp.parameterService));
    map.put("getUnconfirmedTransactions", new GetUnconfirmedTransactions(dp.transactionProcessor, dp.indirectIncomingService, dp.parameterService));
    map.put("getAccountCurrentAskOrderIds", new GetAccountCurrentAskOrderIds(dp.parameterService, dp.assetExchange));
    map.put("getAccountCurrentBidOrderIds", new GetAccountCurrentBidOrderIds(dp.parameterService, dp.assetExchange));
    map.put("getAccountCurrentAskOrders", new GetAccountCurrentAskOrders(dp.parameterService, dp.assetExchange));
    map.put("getAccountCurrentBidOrders", new GetAccountCurrentBidOrders(dp.parameterService, dp.assetExchange));
    map.put("getAllOpenAskOrders", new GetAllOpenAskOrders(dp.assetExchange));
    map.put("getAllOpenBidOrders", new GetAllOpenBidOrders(dp.assetExchange));
    map.put("getAskOrder", new GetAskOrder(dp.assetExchange));
    map.put("getAskOrderIds", new GetAskOrderIds(dp.parameterService, dp.assetExchange));
    map.put("getAskOrders", new GetAskOrders(dp.parameterService, dp.assetExchange));
    map.put("getBidOrder", new GetBidOrder(dp.assetExchange));
    map.put("getBidOrderIds", new GetBidOrderIds(dp.parameterService, dp.assetExchange));
    map.put("getBidOrders", new GetBidOrders(dp.parameterService, dp.assetExchange));
    map.put("suggestFee", new SuggestFee(dp.feeSuggestionCalculator));
    map.put("issueAsset", new IssueAsset(dp));
    map.put("longConvert", LongConvert.Companion.getInstance());
    map.put("parseTransaction", new ParseTransaction(dp.parameterService, dp.transactionService));
    map.put("placeAskOrder", new PlaceAskOrder(dp));
    map.put("placeBidOrder", new PlaceBidOrder(dp));
    map.put("rsConvert", RSConvert.Companion.getInstance());
    map.put("readMessage", new ReadMessage(dp.blockchain, dp.accountService));
    map.put("sendMessage", new SendMessage(dp));
    map.put("sendMoney", new SendMoney(dp));
    map.put("sendMoneyMulti", new SendMoneyMulti(dp));
    map.put("sendMoneyMultiSame", new SendMoneyMultiSame(dp));
    map.put("setAccountInfo", new SetAccountInfo(dp));
    map.put("setAlias", new SetAlias(dp));
    map.put("signTransaction", new SignTransaction(dp.parameterService, dp.transactionService));
    map.put("transferAsset", new TransferAsset(dp));
    map.put("getMiningInfo", new GetMiningInfo(dp));
    map.put("submitNonce", new SubmitNonce(dp.propertyService, dp.accountService, dp.blockchain, dp.generator));
    map.put("getRewardRecipient", new GetRewardRecipient(dp.parameterService, dp.blockchain, dp.accountService));
    map.put("setRewardRecipient", new SetRewardRecipient(dp));
    map.put("getAccountsWithRewardRecipient", new GetAccountsWithRewardRecipient(dp.parameterService, dp.accountService));
    map.put("sendMoneyEscrow", new SendMoneyEscrow(dp));
    map.put("escrowSign", new EscrowSign(dp));
    map.put("getEscrowTransaction", new GetEscrowTransaction(dp.escrowService));
    map.put("getAccountEscrowTransactions", new GetAccountEscrowTransactions(dp.parameterService, dp.escrowService));
    map.put("sendMoneySubscription", new SendMoneySubscription(dp));
    map.put("subscriptionCancel", new SubscriptionCancel(dp));
    map.put("getSubscription", new GetSubscription(dp.subscriptionService));
    map.put("getAccountSubscriptions", new GetAccountSubscriptions(dp.parameterService, dp.subscriptionService));
    map.put("getSubscriptionsToAccount", new GetSubscriptionsToAccount(dp.parameterService, dp.subscriptionService));
    map.put("createATProgram", new CreateATProgram(dp));
    map.put("getAT", new GetAT(dp.parameterService, dp.accountService));
    map.put("getATDetails", new GetATDetails(dp.parameterService, dp.accountService));
    map.put("getATIds", new GetATIds(dp.atService));
    map.put("getATLong", GetATLong.Companion.getInstance());
    map.put("getAccountATs", new GetAccountATs(dp.parameterService, dp.atService, dp.accountService));
    map.put("getGuaranteedBalance", new GetGuaranteedBalance(dp.parameterService));
    map.put("generateSendTransactionQRCode", new GenerateDeeplinkQRCode(dp.deeplinkQRCodeGenerator));

    if (dp.propertyService.get(Props.API_DEBUG)) {
      map.put("clearUnconfirmedTransactions", new ClearUnconfirmedTransactions(dp.transactionProcessor));
      map.put("fullReset", new FullReset(dp.blockchainProcessor));
      map.put("popOff", new PopOff(dp.blockchainProcessor, dp.blockchain, dp.blockService));
    }

    apiRequestHandlers = Collections.unmodifiableMap(map);
  }

  abstract static class JsonRequestHandler extends HttpRequestHandler {
    JsonRequestHandler(APITag[] apiTags, String... parameters) {
      super(apiTags, parameters);
    }

    @Override
    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      long startTime = System.currentTimeMillis();

      JsonElement response;
      try {
        response = processRequest(req);
      } catch (ParameterException e) {
        response = e.getErrorResponse();
      } catch (BurstException | RuntimeException e) {
        logger.debug("Error processing API request", e);
        response = INSTANCE.getERROR_INCORRECT_REQUEST();
      }

      if (response instanceof JsonObject) {
        JSON.getAsJsonObject(response).addProperty("requestProcessingTime", System.currentTimeMillis() - startTime);
      }

      writeJsonToResponse(resp, response);
    }

    abstract JsonElement processRequest(HttpServletRequest request) throws BurstException;
  }

  abstract static class HttpRequestHandler {

    private final List<String> parameters;
    private final Set<APITag> apiTags;

    HttpRequestHandler(APITag[] apiTags, String... parameters) {
      this.parameters = Collections.unmodifiableList(Arrays.asList(parameters));
      this.apiTags = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(apiTags)));
    }

    final List<String> getParameters() {
      return parameters;
    }

    final Set<APITag> getAPITags() {
      return apiTags;
    }

    protected abstract void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException;

    void addErrorMessage(HttpServletResponse resp, JsonElement msg) throws IOException {
      writeJsonToResponse(resp, msg);
    }

    final void validateParams(HttpServletRequest req) throws ParameterException {
      for (String parameter : req.getParameterMap().keySet()) {
        // _ is a parameter used in eg. jquery to avoid caching queries
        if (!this.parameters.contains(parameter) && !parameter.equals("_") && ! parameter.equals("requestType"))
          throw new ParameterException(JSONResponses.INSTANCE.incorrectUnknown(parameter));
      }
    }

    boolean requirePost() {
      return false;
    }
  }

  private static void writeJsonToResponse(HttpServletResponse resp, JsonElement msg) throws IOException {
    resp.setContentType("text/plain; charset=UTF-8");
    try (Writer writer = resp.getWriter()) {
      JSON.writeTo(msg, writer);
    }
  }

  private final boolean enforcePost;
  private final String allowedOrigins;

  public final Map<String, HttpRequestHandler> apiRequestHandlers;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    try {
      process(req, resp);
    } catch (Exception e) { // We don't want to send exception information to client...
      resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
      logger.warn("Error handling GET request", e);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    try {
      process(req, resp);
    } catch (Exception e) { // We don't want to send exception information to client...
      resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
      logger.warn("Error handling GET request", e);
    }
  }

  private void process(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setHeader("Access-Control-Allow-Methods", "GET, POST");
    resp.setHeader("Access-Control-Allow-Origin", allowedOrigins);
    resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
    resp.setHeader("Pragma", "no-cache");
    resp.setDateHeader("Expires", 0);

    if (allowedBotHosts != null) {
      InetAddress remoteAddress = InetAddress.getByName(req.getRemoteHost());
      boolean allowed = false;
      for (Subnet allowedSubnet : allowedBotHosts) {
        if (allowedSubnet.isInNet(remoteAddress)) {
          allowed = true;
          break;
        }
      }
      if (!allowed) {
        resp.setStatus(HttpStatus.FORBIDDEN_403);
        writeJsonToResponse(resp, INSTANCE.getERROR_NOT_ALLOWED());
        return;
      }
    }

    String requestType = req.getParameter("requestType");
    if (requestType == null) {
      resp.setStatus(HttpStatus.NOT_FOUND_404);
      writeJsonToResponse(resp, INSTANCE.getERROR_MISSING_REQUEST());
      return;
    }

    HttpRequestHandler apiRequestHandler = apiRequestHandlers.get(requestType);
    if (apiRequestHandler == null) {
      resp.setStatus(HttpStatus.NOT_FOUND_404);
      writeJsonToResponse(resp, INSTANCE.getERROR_MISSING_REQUEST());
      return;
    }

    if (enforcePost && apiRequestHandler.requirePost() && !"POST".equals(req.getMethod())) {
      resp.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
      writeJsonToResponse(resp, INSTANCE.getERROR_NOT_ALLOWED());
      return;
    }

    try {
      if (!acceptSurplusParams) apiRequestHandler.validateParams(req);
      apiRequestHandler.processRequest(req, resp);
    } catch (ParameterException e) {
      writeJsonToResponse(resp, e.getErrorResponse());
    } catch (RuntimeException e) {
      logger.debug("Error processing API request", e);
      resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
      writeJsonToResponse(resp, INSTANCE.getERROR_INCORRECT_REQUEST());
    }
  }
}
