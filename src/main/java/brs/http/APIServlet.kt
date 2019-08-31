package brs.http

import brs.*
import brs.props.Props
import brs.services.*
import brs.util.JSON
import brs.util.Subnet
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.eclipse.jetty.http.HttpStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException
import java.io.Writer
import java.net.InetAddress
import java.util.*

class APIServlet(dp: DependencyProvider, private val allowedBotHosts: Set<Subnet>?) : HttpServlet() {
    private val acceptSurplusParams: Boolean

    private val enforcePost: Boolean
    private val allowedOrigins: String

    val apiRequestHandlers: Map<String, HttpRequestHandler>

    init { // TODO each one should just take dp
        enforcePost = dp.propertyService.get(Props.API_SERVER_ENFORCE_POST)
        allowedOrigins = dp.propertyService.get(Props.API_ALLOWED_ORIGINS)
        this.acceptSurplusParams = dp.propertyService.get(Props.API_ACCEPT_SURPLUS_PARAMS)

        val map = HashMap<String, HttpRequestHandler>()

        map["broadcastTransaction"] = BroadcastTransaction(dp.transactionProcessor, dp.parameterService, dp.transactionService)
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
        map["generateToken"] = GenerateToken(dp.timeService)
        map["getAccount"] = GetAccount(dp.parameterService, dp.accountService)
        map["getAccountsWithName"] = GetAccountsWithName(dp.accountService)
        map["getAccountBlockIds"] = GetAccountBlockIds(dp.parameterService, dp.blockchain)
        map["getAccountBlocks"] = GetAccountBlocks(dp.blockchain, dp.parameterService, dp.blockService)
        map["getAccountId"] = GetAccountId()
        map["getAccountPublicKey"] = GetAccountPublicKey(dp.parameterService)
        map["getAccountTransactionIds"] = GetAccountTransactionIds(dp.parameterService, dp.blockchain)
        map["getAccountTransactions"] = GetAccountTransactions(dp.parameterService, dp.blockchain)
        map["getAccountLessors"] = GetAccountLessors(dp.parameterService, dp.blockchain)
        map["sellAlias"] = SellAlias(dp)
        map["buyAlias"] = BuyAlias(dp)
        map["getAlias"] = GetAlias(dp.parameterService, dp.aliasService)
        map["getAliases"] = GetAliases(dp.parameterService, dp.aliasService)
        map["getAllAssets"] = GetAllAssets(dp.assetExchange)
        map["getAsset"] = GetAsset(dp.parameterService, dp.assetExchange)
        map["getAssets"] = GetAssets(dp.assetExchange)
        map["getAssetIds"] = GetAssetIds(dp.assetExchange)
        map["getAssetsByIssuer"] = GetAssetsByIssuer(dp.parameterService, dp.assetExchange)
        map["getAssetAccounts"] = GetAssetAccounts(dp.parameterService, dp.assetExchange)
        map["getBalance"] = GetBalance(dp.parameterService)
        map["getBlock"] = GetBlock(dp.blockchain, dp.blockService)
        map["getBlockId"] = GetBlockId(dp.blockchain)
        map["getBlocks"] = GetBlocks(dp.blockchain, dp.blockService)
        map["getdp.blockchainStatus"] = GetBlockchainStatus(dp.blockchainProcessor, dp.blockchain, dp.timeService)
        map["getConstants"] = GetConstants(dp)
        map["getDGSGoods"] = GetDGSGoods(dp.digitalGoodsStoreService)
        map["getDGSGood"] = GetDGSGood(dp.parameterService)
        map["getDGSPurchases"] = GetDGSPurchases(dp.digitalGoodsStoreService)
        map["getDGSPurchase"] = GetDGSPurchase(dp.parameterService)
        map["getDGSPendingPurchases"] = GetDGSPendingPurchases(dp.digitalGoodsStoreService)
        map["getECBlock"] = GetECBlock(dp.blockchain, dp.timeService, dp.economicClustering)
        map["getMyInfo"] = GetMyInfo.instance
        map["getPeer"] = GetPeer.instance
        map["getMyPeerInfo"] = GetMyPeerInfo(dp.transactionProcessor)
        map["getPeers"] = GetPeers.instance
        map["getState"] = GetState(dp.blockchain, dp.blockchainProcessor, dp.assetExchange, dp.accountService, dp.escrowService, dp.aliasService, dp.timeService, dp.generator, dp.propertyService)
        map["getTime"] = GetTime(dp.timeService)
        map["getTrades"] = GetTrades(dp.parameterService, dp.assetExchange)
        map["getAllTrades"] = GetAllTrades(dp.assetExchange)
        map["getAssetTransfers"] = GetAssetTransfers(dp.parameterService, dp.accountService, dp.assetExchange)
        map["getTransaction"] = GetTransaction(dp.transactionProcessor, dp.blockchain)
        map["getTransactionBytes"] = GetTransactionBytes(dp.blockchain, dp.transactionProcessor)
        map["getUnconfirmedTransactionIds"] = GetUnconfirmedTransactionIds(dp.transactionProcessor, dp.indirectIncomingService, dp.parameterService)
        map["getUnconfirmedTransactions"] = GetUnconfirmedTransactions(dp.transactionProcessor, dp.indirectIncomingService, dp.parameterService)
        map["getAccountCurrentAskOrderIds"] = GetAccountCurrentAskOrderIds(dp.parameterService, dp.assetExchange)
        map["getAccountCurrentBidOrderIds"] = GetAccountCurrentBidOrderIds(dp.parameterService, dp.assetExchange)
        map["getAccountCurrentAskOrders"] = GetAccountCurrentAskOrders(dp.parameterService, dp.assetExchange)
        map["getAccountCurrentBidOrders"] = GetAccountCurrentBidOrders(dp.parameterService, dp.assetExchange)
        map["getAllOpenAskOrders"] = GetAllOpenAskOrders(dp.assetExchange)
        map["getAllOpenBidOrders"] = GetAllOpenBidOrders(dp.assetExchange)
        map["getAskOrder"] = GetAskOrder(dp.assetExchange)
        map["getAskOrderIds"] = GetAskOrderIds(dp.parameterService, dp.assetExchange)
        map["getAskOrders"] = GetAskOrders(dp.parameterService, dp.assetExchange)
        map["getBidOrder"] = GetBidOrder(dp.assetExchange)
        map["getBidOrderIds"] = GetBidOrderIds(dp.parameterService, dp.assetExchange)
        map["getBidOrders"] = GetBidOrders(dp.parameterService, dp.assetExchange)
        map["suggestFee"] = SuggestFee(dp.feeSuggestionCalculator)
        map["issueAsset"] = IssueAsset(dp)
        map["longConvert"] = LongConvert.instance
        map["parseTransaction"] = ParseTransaction(dp.parameterService, dp.transactionService)
        map["placeAskOrder"] = PlaceAskOrder(dp)
        map["placeBidOrder"] = PlaceBidOrder(dp)
        map["rsConvert"] = RSConvert.instance
        map["readMessage"] = ReadMessage(dp.blockchain, dp.accountService)
        map["sendMessage"] = SendMessage(dp)
        map["sendMoney"] = SendMoney(dp)
        map["sendMoneyMulti"] = SendMoneyMulti(dp)
        map["sendMoneyMultiSame"] = SendMoneyMultiSame(dp)
        map["setAccountInfo"] = SetAccountInfo(dp)
        map["setAlias"] = SetAlias(dp)
        map["signTransaction"] = SignTransaction(dp.parameterService, dp.transactionService)
        map["transferAsset"] = TransferAsset(dp)
        map["getMiningInfo"] = GetMiningInfo(dp)
        map["submitNonce"] = SubmitNonce(dp.propertyService, dp.accountService, dp.blockchain, dp.generator)
        map["getRewardRecipient"] = GetRewardRecipient(dp.parameterService, dp.blockchain, dp.accountService)
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
        map["getATLong"] = GetATLong.instance
        map["getAccountATs"] = GetAccountATs(dp.parameterService, dp.atService, dp.accountService)
        map["getGuaranteedBalance"] = GetGuaranteedBalance(dp.parameterService)
        map["generateSendTransactionQRCode"] = GenerateDeeplinkQRCode(dp.deeplinkQRCodeGenerator)

        if (dp.propertyService.get(Props.API_DEBUG)) {
            map["clearUnconfirmedTransactions"] = ClearUnconfirmedTransactions(dp.transactionProcessor)
            map["fullReset"] = FullReset(dp.blockchainProcessor)
            map["popOff"] = PopOff(dp.blockchainProcessor, dp.blockchain, dp.blockService)
        }

        apiRequestHandlers = Collections.unmodifiableMap(map)
    }

    internal abstract class JsonRequestHandler(apiTags: Array<APITag>, vararg parameters: String) : HttpRequestHandler(apiTags, parameters) {

        @Throws(IOException::class)
        protected override fun processRequest(req: HttpServletRequest, resp: HttpServletResponse) {
            val startTime = System.currentTimeMillis()

            var response: JsonElement
            try {
                response = processRequest(req)
            } catch (e: ParameterException) {
                response = e.errorResponse
            } catch (e: BurstException) {
                logger.debug("Error processing API request", e)
                response = INSTANCE.ERROR_INCORRECT_REQUEST
            } catch (e: RuntimeException) {
                logger.debug("Error processing API request", e)
                response = INSTANCE.ERROR_INCORRECT_REQUEST
            }

            if (response is JsonObject) {
                JSON.getAsJsonObject(response).addProperty("requestProcessingTime", System.currentTimeMillis() - startTime)
            }

            writeJsonToResponse(resp, response)
        }

        @Throws(BurstException::class)
        internal abstract fun processRequest(request: HttpServletRequest): JsonElement
    }

    internal abstract class HttpRequestHandler(apiTags: Array<APITag>, vararg parameters: String) {

        val parameters: List<String>
        val apiTags: Set<APITag>

        init {
            this.parameters = Collections.unmodifiableList(Arrays.asList(*parameters))
            this.apiTags = Collections.unmodifiableSet(HashSet(Arrays.asList(*apiTags)))
        }

        @Throws(IOException::class)
        abstract fun processRequest(req: HttpServletRequest, resp: HttpServletResponse)

        @Throws(IOException::class)
        fun addErrorMessage(resp: HttpServletResponse, msg: JsonElement) {
            writeJsonToResponse(resp, msg)
        }

        @Throws(ParameterException::class)
        fun validateParams(req: HttpServletRequest) {
            for (parameter in req.parameterMap.keys) {
                // _ is a parameter used in eg. jquery to avoid caching queries
                if (!this.parameters.contains(parameter) && parameter != "_" && parameter != "requestType")
                    throw ParameterException(JSONResponses.incorrectUnknown(parameter))
            }
        }

        fun requirePost(): Boolean {
            return false
        }
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        try {
            process(req, resp)
        } catch (e: Exception) { // We don't want to send exception information to client...
            resp.status = HttpStatus.INTERNAL_SERVER_ERROR_500
            logger.warn("Error handling GET request", e)
        }

    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        try {
            process(req, resp)
        } catch (e: Exception) { // We don't want to send exception information to client...
            resp.status = HttpStatus.INTERNAL_SERVER_ERROR_500
            logger.warn("Error handling GET request", e)
        }

    }

    @Throws(IOException::class)
    private fun process(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST")
        resp.setHeader("Access-Control-Allow-Origin", allowedOrigins)
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private")
        resp.setHeader("Pragma", "no-cache")
        resp.setDateHeader("Expires", 0)

        if (allowedBotHosts != null) {
            val remoteAddress = InetAddress.getByName(req.remoteHost)
            var allowed = false
            for (allowedSubnet in allowedBotHosts) {
                if (allowedSubnet.isInNet(remoteAddress)) {
                    allowed = true
                    break
                }
            }
            if (!allowed) {
                resp.status = HttpStatus.FORBIDDEN_403
                writeJsonToResponse(resp, INSTANCE.ERROR_NOT_ALLOWED)
                return
            }
        }

        val requestType = req.getParameter("requestType")
        if (requestType == null) {
            resp.status = HttpStatus.NOT_FOUND_404
            writeJsonToResponse(resp, INSTANCE.ERROR_MISSING_REQUEST)
            return
        }

        val apiRequestHandler = apiRequestHandlers[requestType]
        if (apiRequestHandler == null) {
            resp.status = HttpStatus.NOT_FOUND_404
            writeJsonToResponse(resp, INSTANCE.ERROR_MISSING_REQUEST)
            return
        }

        if (enforcePost && apiRequestHandler.requirePost() && "POST" != req.method) {
            resp.status = HttpStatus.METHOD_NOT_ALLOWED_405
            writeJsonToResponse(resp, INSTANCE.ERROR_NOT_ALLOWED)
            return
        }

        try {
            if (!acceptSurplusParams) apiRequestHandler.validateParams(req)
            apiRequestHandler.processRequest(req, resp)
        } catch (e: ParameterException) {
            writeJsonToResponse(resp, e.errorResponse)
        } catch (e: RuntimeException) {
            logger.debug("Error processing API request", e)
            resp.status = HttpStatus.INTERNAL_SERVER_ERROR_500
            writeJsonToResponse(resp, INSTANCE.ERROR_INCORRECT_REQUEST)
        }

    }

    companion object {

        private val logger = LoggerFactory.getLogger(APIServlet::class.java)

        @Throws(IOException::class)
        private fun writeJsonToResponse(resp: HttpServletResponse, msg: JsonElement) {
            resp.contentType = "text/plain; charset=UTF-8"
            resp.writer.use { writer -> JSON.writeTo(msg, writer) }
        }
    }
}
