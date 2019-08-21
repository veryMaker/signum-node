package brs.http

import brs.*
import brs.assetexchange.AssetExchange
import brs.peer.Peers
import brs.props.PropertyService
import brs.props.Props
import brs.services.AccountService
import brs.services.AliasService
import brs.services.EscrowService
import brs.services.TimeService
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest

import brs.http.common.Parameters.INCLUDE_COUNTS_PARAMETER
import brs.http.common.ResultFields.TIME_RESPONSE

internal class GetState(private val blockchain: Blockchain, private val blockchainProcessor: BlockchainProcessor, private val assetExchange: AssetExchange, private val accountService: AccountService, private val escrowService: EscrowService,
                        private val aliasService: AliasService, private val timeService: TimeService, private val generator: Generator, private val propertyService: PropertyService) : APIServlet.JsonRequestHandler(arrayOf(APITag.INFO), INCLUDE_COUNTS_PARAMETER) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val response = JsonObject()

        response.addProperty("application", Burst.APPLICATION)
        response.addProperty("version", Burst.VERSION.toString())
        response.addProperty(TIME_RESPONSE, timeService.epochTime)
        response.addProperty("lastBlock", blockchain.lastBlock.stringId)
        response.addProperty("cumulativeDifficulty", blockchain.lastBlock.cumulativeDifficulty.toString())

        if (!"false".equals(req.getParameter("includeCounts"), ignoreCase = true)) {
            var totalEffectiveBalance: Long = 0
            for (account in accountService.getAllAccounts(0, -1)) {
                val effectiveBalanceBURST = account.balanceNQT
                if (effectiveBalanceBURST > 0) {
                    totalEffectiveBalance += effectiveBalanceBURST
                }
            }
            for (escrow in escrowService.allEscrowTransactions) {
                totalEffectiveBalance += escrow.getAmountNQT()!!
            }
            response.addProperty("totalEffectiveBalanceNXT", totalEffectiveBalance / Constants.ONE_BURST)

            response.addProperty("numberOfBlocks", blockchain.height + 1)
            response.addProperty("numberOfTransactions", blockchain.transactionCount)
            response.addProperty("numberOfAccounts", accountService.count)
            response.addProperty("numberOfAssets", assetExchange.assetsCount)
            val askCount = assetExchange.askCount
            val bidCount = assetExchange.bidCount
            response.addProperty("numberOfOrders", askCount + bidCount)
            response.addProperty("numberOfAskOrders", askCount)
            response.addProperty("numberOfBidOrders", bidCount)
            response.addProperty("numberOfTrades", assetExchange.tradesCount)
            response.addProperty("numberOfTransfers", assetExchange.assetTransferCount)
            response.addProperty("numberOfAliases", aliasService.aliasCount)
        }
        response.addProperty("numberOfPeers", Peers.getAllPeers().size)
        response.addProperty("numberOfUnlockedAccounts", generator.allGenerators.size)
        response.addProperty("lastBlockchainFeeder", blockchainProcessor.lastBlockchainFeeder?.announcedAddress)
        response.addProperty("lastBlockchainFeederHeight", blockchainProcessor.lastBlockchainFeederHeight)
        response.addProperty("isScanning", blockchainProcessor.isScanning)
        response.addProperty("availableProcessors", Runtime.getRuntime().availableProcessors())
        response.addProperty("maxMemory", Runtime.getRuntime().maxMemory())
        response.addProperty("totalMemory", Runtime.getRuntime().totalMemory())
        response.addProperty("freeMemory", Runtime.getRuntime().freeMemory())
        response.addProperty("indirectIncomingServiceEnabled", propertyService.get(Props.INDIRECT_INCOMING_SERVICE_ENABLE))
        val grpcApiEnabled = propertyService.get(Props.API_V2_SERVER)!!
        response.addProperty("grpcApiEnabled", grpcApiEnabled)
        if (grpcApiEnabled) response.addProperty("grpcApiPort", propertyService.get(if (propertyService.get(Props.DEV_TESTNET)) Props.DEV_API_V2_PORT else Props.API_V2_PORT))

        return response
    }
}
