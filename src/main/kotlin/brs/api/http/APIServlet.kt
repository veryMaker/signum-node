package brs.api.http

import brs.api.http.JSONResponses.ERROR_INCORRECT_REQUEST
import brs.api.http.JSONResponses.ERROR_MISSING_REQUEST
import brs.api.http.JSONResponses.ERROR_NOT_ALLOWED
import brs.entity.DependencyProvider
import brs.objects.Props
import brs.util.Subnet
import brs.util.json.mustGetAsJsonObject
import brs.util.json.writeTo
import brs.util.logging.safeDebug
import brs.util.logging.safeWarn
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.eclipse.jetty.http.HttpStatus
import org.slf4j.LoggerFactory
import java.net.InetAddress
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class APIServlet(dp: DependencyProvider, private val allowedBotHosts: Set<Subnet>?) : HttpServlet() {
    private val acceptSurplusParams = dp.propertyService.get(Props.API_ACCEPT_SURPLUS_PARAMS)
    private val enforcePost = dp.propertyService.get(Props.API_SERVER_ENFORCE_POST)
    private val allowedOrigins = dp.propertyService.get(Props.API_ALLOWED_ORIGINS)

    internal val apiRequestHandlers: Map<String, HttpRequestHandler>

    init { // TODO each one should just take dp
        val map = mutableMapOf<String, HttpRequestHandler>()
        map["broadcastTransaction"] =
            BroadcastTransaction(dp.transactionProcessorService, dp.parameterService, dp.transactionService)
        map["calculateFullHash"] = CalculateFullHash()
        map["cancelAskOrder"] = CancelAskOrder(dp)
        map["cancelBidOrder"] = CancelBidOrder(dp)
        map["decryptFrom"] = DecryptFrom(dp.parameterService)
        map["dgsListing"] = DGSListing(dp)
        map["dgsDelisting"] = DGSDelisting(dp)
        map["dgsDelivery"] = DGSDelivery(dp)
        map["dgsFeedback"] = DGSFeedback(dp)
        map["dgsPriceChange"] = DGSPriceChange(dp)
        map["dgsPurchase"] = DGSPurchase(dp)
        map["dgsQuantityChange"] = DGSQuantityChange(dp)
        map["dgsRefund"] = DGSRefund(dp)
        map["encryptTo"] = EncryptTo(dp.parameterService, dp.accountService)
        map["getAccount"] = GetAccount(dp.parameterService, dp.accountService)
        map["getAccountsWithName"] = GetAccountsWithName(dp.accountService)
        map["getAccountBlockIds"] = GetAccountBlockIds(dp.parameterService, dp.blockchainService)
        map["getAccountBlocks"] = GetAccountBlocks(dp.blockchainService, dp.parameterService, dp.blockService)
        map["getAccountId"] = GetAccountId()
        map["getAccountPublicKey"] = GetAccountPublicKey(dp.parameterService)
        map["getAccountTransactionIds"] = GetAccountTransactionIds(dp.parameterService, dp.blockchainService)
        map["getAccountTransactions"] = GetAccountTransactions(dp.parameterService, dp.blockchainService)
        map["sellAlias"] = SellAlias(dp)
        map["buyAlias"] = BuyAlias(dp)
        map["getAlias"] = GetAlias(dp.parameterService, dp.aliasService)
        map["getAliases"] = GetAliases(dp.parameterService, dp.aliasService)
        map["getAllAssets"] = GetAllAssets(dp.assetExchangeService)
        map["getAsset"] = GetAsset(dp.parameterService, dp.assetExchangeService)
        map["getAssets"] = GetAssets(dp.assetExchangeService)
        map["getAssetIds"] = GetAssetIds(dp.assetExchangeService)
        map["getAssetsByIssuer"] = GetAssetsByIssuer(dp.parameterService, dp.assetExchangeService)
        map["getAssetAccounts"] = GetAssetAccounts(dp.parameterService, dp.assetExchangeService)
        map["getBalance"] = GetBalance(dp.parameterService)
        map["getBlock"] = GetBlock(dp.blockchainService, dp.blockService)
        map["getBlockId"] = GetBlockId(dp.blockchainService)
        map["getBlocks"] = GetBlocks(dp.blockchainService, dp.blockService)
        map["getBlockchainStatus"] =
            GetBlockchainStatus(dp.blockchainProcessorService, dp.blockchainService, dp.timeService)
        map["getConstants"] = GetConstants(dp)
        map["getDGSGoods"] = GetDGSGoods(dp.digitalGoodsStoreService)
        map["getDGSGood"] = GetDGSGood(dp.parameterService)
        map["getDGSPurchases"] = GetDGSPurchases(dp.digitalGoodsStoreService)
        map["getDGSPurchase"] = GetDGSPurchase(dp.parameterService)
        map["getDGSPendingPurchases"] = GetDGSPendingPurchases(dp.digitalGoodsStoreService)
        map["getECBlock"] = GetECBlock(dp.blockchainService, dp.timeService, dp.economicClusteringService)
        map["getMyInfo"] = GetMyInfo
        map["getPeer"] = GetPeer(dp)
        map["getMyPeerInfo"] = GetMyPeerInfo(dp)
        map["getPeers"] = GetPeers(dp)
        map["getState"] = GetState(dp)
        map["getTime"] = GetTime(dp.timeService)
        map["getTrades"] = GetTrades(dp.parameterService, dp.assetExchangeService)
        map["getAllTrades"] = GetAllTrades(dp.assetExchangeService)
        map["getAssetTransfers"] = GetAssetTransfers(dp.parameterService, dp.accountService, dp.assetExchangeService)
        map["getTransaction"] = GetTransaction(dp.blockchainService)
        map["getTransactionBytes"] = GetTransactionBytes(dp)
        map["getUnconfirmedTransactionIds"] = GetUnconfirmedTransactionIds(dp)
        map["getUnconfirmedTransactions"] =
            GetUnconfirmedTransactions(dp)
        map["getAccountCurrentAskOrderIds"] = GetAccountCurrentAskOrderIds(dp.parameterService, dp.assetExchangeService)
        map["getAccountCurrentBidOrderIds"] = GetAccountCurrentBidOrderIds(dp.parameterService, dp.assetExchangeService)
        map["getAccountCurrentAskOrders"] = GetAccountCurrentAskOrders(dp.parameterService, dp.assetExchangeService)
        map["getAccountCurrentBidOrders"] = GetAccountCurrentBidOrders(dp.parameterService, dp.assetExchangeService)
        map["getAllOpenAskOrders"] = GetAllOpenAskOrders(dp.assetExchangeService)
        map["getAllOpenBidOrders"] = GetAllOpenBidOrders(dp.assetExchangeService)
        map["getAskOrder"] = GetAskOrder(dp.assetExchangeService)
        map["getAskOrderIds"] = GetAskOrderIds(dp.parameterService, dp.assetExchangeService)
        map["getAskOrders"] = GetAskOrders(dp.parameterService, dp.assetExchangeService)
        map["getBidOrder"] = GetBidOrder(dp.assetExchangeService)
        map["getBidOrderIds"] = GetBidOrderIds(dp.parameterService, dp.assetExchangeService)
        map["getBidOrders"] = GetBidOrders(dp.parameterService, dp.assetExchangeService)
        map["suggestFee"] = SuggestFee(dp.feeSuggestionService)
        map["issueAsset"] = IssueAsset(dp)
        map["longConvert"] = LongConvert
        map["parseTransaction"] = ParseTransaction(dp.parameterService, dp.transactionService)
        map["placeAskOrder"] = PlaceAskOrder(dp)
        map["placeBidOrder"] = PlaceBidOrder(dp)
        map["rsConvert"] = RSConvert
        map["readMessage"] = ReadMessage(dp.blockchainService, dp.accountService)
        map["sendMessage"] = SendMessage(dp)
        map["sendMoney"] = SendMoney(dp)
        map["sendMoneyMulti"] = SendMoneyMulti(dp)
        map["sendMoneyMultiSame"] = SendMoneyMultiSame(dp)
        map["setAccountInfo"] = SetAccountInfo(dp)
        map["setAlias"] = SetAlias(dp)
        map["signTransaction"] = SignTransaction(dp.parameterService, dp.transactionService)
        map["transferAsset"] = TransferAsset(dp)
        map["getMiningInfo"] = GetMiningInfo(dp)
        map["submitNonce"] =
            SubmitNonce(dp.propertyService, dp.accountService, dp.blockchainService, dp.generatorService)
        map["getRewardRecipient"] = GetRewardRecipient(dp.parameterService, dp.blockchainService, dp.accountService)
        map["setRewardRecipient"] = SetRewardRecipient(dp)
        map["getAccountsWithRewardRecipient"] = GetAccountsWithRewardRecipient(dp.parameterService, dp.accountService)
        map["sendMoneyEscrow"] = SendMoneyEscrow(dp)
        map["escrowSign"] = EscrowSign(dp)
        map["getEscrowTransaction"] = GetEscrowTransaction(dp.escrowService)
        map["getAccountEscrowTransactions"] = GetAccountEscrowTransactions(dp.parameterService, dp.escrowService)
        map["sendMoneySubscription"] = SendMoneySubscription(dp)
        map["subscriptionCancel"] = SubscriptionCancel(dp)
        map["getSubscription"] = GetSubscription(dp.subscriptionService)
        map["getAccountSubscriptions"] = GetAccountSubscriptions(dp.parameterService, dp.subscriptionService)
        map["getSubscriptionsToAccount"] = GetSubscriptionsToAccount(dp.parameterService, dp.subscriptionService)
        map["createATProgram"] = CreateATProgram(dp)
        map["getAT"] = GetAT(dp.parameterService, dp.accountService)
        map["getATDetails"] = GetATDetails(dp.parameterService, dp.accountService)
        map["getATIds"] = GetATIds(dp.atService)
        map["getATLong"] = GetATLong
        map["getAccountATs"] = GetAccountATs(dp.parameterService, dp.atService, dp.accountService)
        map["getGuaranteedBalance"] = GetGuaranteedBalance(dp.parameterService)
        map["generateSendTransactionQRCode"] = GenerateDeeplinkQRCode(dp.deeplinkQRCodeGeneratorService)
        map["generateDeeplink"] = GenerateDeeplink(dp.deeplinkGeneratorService)
        map["generateDeeplinkQRCode"] = GenerateDeeplinkQR(dp)
        if (dp.propertyService.get(Props.API_DEBUG)) {
            map["clearUnconfirmedTransactions"] = ClearUnconfirmedTransactions(dp.transactionProcessorService)
            map["fullReset"] = FullReset(dp.blockchainProcessorService)
            map["popOff"] = PopOff(dp.blockchainProcessorService, dp.blockchainService, dp.blockService)
        }
        apiRequestHandlers = map
    }

    internal abstract class JsonRequestHandler(apiTags: Array<APITag>, vararg parameters: String) :
        HttpRequestHandler(apiTags, *parameters) {
        override fun processRequest(request: HttpServletRequest, resp: HttpServletResponse) {
            val startTime = System.currentTimeMillis()

            val response = try {
                processRequest(request)
            } catch (e: ParameterException) {
                e.errorResponse
            } catch (e: Exception) {
                logger.safeDebug(e) { "Error processing API request" }
                ERROR_INCORRECT_REQUEST
            }

            if (response is JsonObject) {
                response.mustGetAsJsonObject("response")
                    .addProperty("requestProcessingTime", System.currentTimeMillis() - startTime)
            }

            writeJsonToResponse(resp, response)
        }

        internal abstract fun processRequest(request: HttpServletRequest): JsonElement
    }

    internal abstract class HttpRequestHandler(apiTags: Array<APITag>, vararg parameters: String) {

        val parameters = parameters.toList()
        val apiTags = apiTags.toSet()

        abstract fun processRequest(request: HttpServletRequest, resp: HttpServletResponse)

        fun addErrorMessage(resp: HttpServletResponse, msg: JsonElement) {
            writeJsonToResponse(resp, msg)
        }

        fun validateParams(request: HttpServletRequest) {
            for (parameter in request.parameterMap.keys) {
                // _ is a parameter used in eg. jquery to avoid caching queries
                if (!this.parameters.contains(parameter) && parameter != "_" && parameter != "requestType")
                    throw ParameterException(JSONResponses.incorrectUnknown(parameter))
            }
        }

        internal open fun requirePost(): Boolean {
            return false
        }
    }

    override fun doGet(request: HttpServletRequest, resp: HttpServletResponse) {
        try {
            process(request, resp)
        } catch (e: Exception) { // We don't want to send exception information to client...
            resp.status = HttpStatus.INTERNAL_SERVER_ERROR_500
            logger.safeWarn(e) { "Error handling GET request" }
        }

    }

    override fun doPost(request: HttpServletRequest, resp: HttpServletResponse) {
        try {
            process(request, resp)
        } catch (e: Exception) { // We don't want to send exception information to client...
            resp.status = HttpStatus.INTERNAL_SERVER_ERROR_500
            logger.safeWarn(e) { "Error handling GET request" }
        }

    }

    private fun process(request: HttpServletRequest, resp: HttpServletResponse) {
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST")
        resp.setHeader("Access-Control-Allow-Origin", allowedOrigins)
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private")
        resp.setHeader("Pragma", "no-cache")
        resp.setDateHeader("Expires", 0)

        if (allowedBotHosts != null) {
            val remoteAddress = InetAddress.getByName(request.remoteHost)
            var allowed = false
            for (allowedSubnet in allowedBotHosts) {
                if (allowedSubnet.isInNet(remoteAddress)) {
                    allowed = true
                    break
                }
            }
            if (!allowed) {
                resp.status = HttpStatus.FORBIDDEN_403
                writeJsonToResponse(resp, ERROR_NOT_ALLOWED)
                return
            }
        }

        val requestType = request.getParameter("requestType")
        if (requestType == null) {
            resp.status = HttpStatus.NOT_FOUND_404
            writeJsonToResponse(resp, ERROR_MISSING_REQUEST)
            return
        }

        val apiRequestHandler = apiRequestHandlers[requestType]
        if (apiRequestHandler == null) {
            resp.status = HttpStatus.NOT_FOUND_404
            writeJsonToResponse(resp, ERROR_MISSING_REQUEST)
            return
        }

        if (enforcePost && apiRequestHandler.requirePost() && "POST" != request.method) {
            resp.status = HttpStatus.METHOD_NOT_ALLOWED_405
            writeJsonToResponse(resp, ERROR_NOT_ALLOWED)
            return
        }

        try {
            if (!acceptSurplusParams) apiRequestHandler.validateParams(request)
            apiRequestHandler.processRequest(request, resp)
        } catch (e: ParameterException) {
            writeJsonToResponse(resp, e.errorResponse)
        } catch (e: Exception) {
            logger.safeDebug(e) { "Error processing API request" }
            resp.status = HttpStatus.INTERNAL_SERVER_ERROR_500
            writeJsonToResponse(resp, ERROR_INCORRECT_REQUEST)
        }

    }

    companion object {
        private val logger = LoggerFactory.getLogger(APIServlet::class.java)

        private fun writeJsonToResponse(resp: HttpServletResponse, msg: JsonElement) {
            resp.contentType = "text/plain; charset=UTF-8"
            resp.writer.use { writer -> msg.writeTo(writer) }
        }
    }
}
