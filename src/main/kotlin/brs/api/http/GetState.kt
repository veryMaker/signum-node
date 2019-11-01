package brs.api.http

import brs.Burst
import brs.objects.Constants
import brs.entity.DependencyProvider
import brs.api.http.common.Parameters.INCLUDE_COUNTS_PARAMETER
import brs.api.http.common.ResultFields.TIME_RESPONSE
import brs.objects.Props
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetState(private val dp: DependencyProvider) :
    APIServlet.JsonRequestHandler(arrayOf(APITag.INFO), INCLUDE_COUNTS_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        val response = JsonObject()

        response.addProperty("application", Burst.APPLICATION)
        response.addProperty("version", Burst.VERSION.toString())
        response.addProperty(TIME_RESPONSE, dp.timeService.epochTime)
        response.addProperty("lastBlock", dp.blockchainService.lastBlock.stringId)
        response.addProperty("cumulativeDifficulty", dp.blockchainService.lastBlock.cumulativeDifficulty.toString())

        if (!"false".equals(request.getParameter("includeCounts"), ignoreCase = true)) {
            var totalEffectiveBalance: Long = 0
            for (account in dp.accountService.getAllAccounts(0, -1)) {
                val effectiveBalanceBURST = account.balancePlanck
                if (effectiveBalanceBURST > 0) {
                    totalEffectiveBalance += effectiveBalanceBURST
                }
            }
            for (escrow in dp.escrowService.getAllEscrowTransactions()) {
                totalEffectiveBalance += escrow.amountPlanck
            }
            response.addProperty("totalEffectiveBalanceNXT", totalEffectiveBalance / Constants.ONE_BURST)

            response.addProperty("numberOfBlocks", dp.blockchainService.height + 1)
            response.addProperty("numberOfTransactions", dp.blockchainService.getTransactionCount())
            response.addProperty("numberOfAccounts", dp.accountService.count)
            response.addProperty("numberOfAssets", dp.assetExchangeService.assetsCount)
            val askCount = dp.assetExchangeService.askCount
            val bidCount = dp.assetExchangeService.bidCount
            response.addProperty("numberOfOrders", askCount + bidCount)
            response.addProperty("numberOfAskOrders", askCount)
            response.addProperty("numberOfBidOrders", bidCount)
            response.addProperty("numberOfTrades", dp.assetExchangeService.tradesCount)
            response.addProperty("numberOfTransfers", dp.assetExchangeService.assetTransferCount)
            response.addProperty("numberOfAliases", dp.aliasService.getAliasCount())
        }
        response.addProperty("numberOfPeers", dp.peerService.allPeers.size)
        response.addProperty("numberOfUnlockedAccounts", dp.generatorService.allGenerators.size)
        response.addProperty(
            "lastBlockchainFeeder",
            dp.blockchainProcessorService.lastBlockchainFeeder?.announcedAddress
        )
        response.addProperty("lastBlockchainFeederHeight", dp.blockchainProcessorService.lastBlockchainFeederHeight)
        response.addProperty("availableProcessors", Runtime.getRuntime().availableProcessors())
        response.addProperty("maxMemory", Runtime.getRuntime().maxMemory())
        response.addProperty("totalMemory", Runtime.getRuntime().totalMemory())
        response.addProperty("freeMemory", Runtime.getRuntime().freeMemory())
        response.addProperty(
            "indirectIncomingServiceEnabled",
            dp.propertyService.get(Props.INDIRECT_INCOMING_SERVICE_ENABLE)
        )
        val grpcApiEnabled = dp.propertyService.get(Props.API_V2_SERVER)
        response.addProperty("grpcApiEnabled", grpcApiEnabled)
        if (grpcApiEnabled) response.addProperty(
            "grpcApiPort",
            dp.propertyService.get(if (dp.propertyService.get(Props.DEV_TESTNET)) Props.DEV_API_V2_PORT else Props.API_V2_PORT)
        )

        return response
    }
}
