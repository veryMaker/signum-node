package brs.http

import brs.Burst
import brs.Constants
import brs.DependencyProvider
import brs.http.common.Parameters.INCLUDE_COUNTS_PARAMETER
import brs.http.common.ResultFields.TIME_RESPONSE
import brs.props.Props
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetState(private val dp: DependencyProvider) : APIServlet.JsonRequestHandler(arrayOf(APITag.INFO), INCLUDE_COUNTS_PARAMETER) {
    internal override fun processRequest(request: HttpServletRequest): JsonElement {
        val response = JsonObject()

        response.addProperty("application", Burst.APPLICATION)
        response.addProperty("version", Burst.VERSION.toString())
        response.addProperty(TIME_RESPONSE, dp.timeService.epochTime)
        response.addProperty("lastBlock", dp.blockchain.lastBlock.stringId)
        response.addProperty("cumulativeDifficulty", dp.blockchain.lastBlock.cumulativeDifficulty.toString())

        if (!"false".equals(request.getParameter("includeCounts"), ignoreCase = true)) {
            var totalEffectiveBalance: Long = 0
            for (account in dp.accountService.getAllAccounts(0, -1)) {
                val effectiveBalanceBURST = account.balanceNQT
                if (effectiveBalanceBURST > 0) {
                    totalEffectiveBalance += effectiveBalanceBURST
                }
            }
            for (escrow in dp.escrowService.allEscrowTransactions) {
                totalEffectiveBalance += escrow.amountNQT!!
            }
            response.addProperty("totalEffectiveBalanceNXT", totalEffectiveBalance / Constants.ONE_BURST)

            response.addProperty("numberOfBlocks", dp.blockchain.height + 1)
            response.addProperty("numberOfTransactions", dp.blockchain.transactionCount)
            response.addProperty("numberOfAccounts", dp.accountService.count)
            response.addProperty("numberOfAssets", dp.assetExchange.assetsCount)
            val askCount = dp.assetExchange.askCount
            val bidCount = dp.assetExchange.bidCount
            response.addProperty("numberOfOrders", askCount + bidCount)
            response.addProperty("numberOfAskOrders", askCount)
            response.addProperty("numberOfBidOrders", bidCount)
            response.addProperty("numberOfTrades", dp.assetExchange.tradesCount)
            response.addProperty("numberOfTransfers", dp.assetExchange.assetTransferCount)
            response.addProperty("numberOfAliases", dp.aliasService.aliasCount)
        }
        response.addProperty("numberOfPeers", dp.peers.allPeers.size)
        response.addProperty("numberOfUnlockedAccounts", dp.generator.allGenerators.size)
        response.addProperty("lastBlockchainFeeder", dp.blockchainProcessor.lastBlockchainFeeder?.announcedAddress)
        response.addProperty("lastBlockchainFeederHeight", dp.blockchainProcessor.lastBlockchainFeederHeight)
        response.addProperty("availableProcessors", Runtime.getRuntime().availableProcessors())
        response.addProperty("maxMemory", Runtime.getRuntime().maxMemory())
        response.addProperty("totalMemory", Runtime.getRuntime().totalMemory())
        response.addProperty("freeMemory", Runtime.getRuntime().freeMemory())
        response.addProperty("indirectIncomingServiceEnabled", dp.propertyService.get(Props.INDIRECT_INCOMING_SERVICE_ENABLE))
        val grpcApiEnabled = dp.propertyService.get(Props.API_V2_SERVER)
        response.addProperty("grpcApiEnabled", grpcApiEnabled)
        if (grpcApiEnabled) response.addProperty("grpcApiPort", dp.propertyService.get(if (dp.propertyService.get(Props.DEV_TESTNET)) Props.DEV_API_V2_PORT else Props.API_V2_PORT))

        return response
    }
}
