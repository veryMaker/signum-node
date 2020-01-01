package brs.api.grpc.http

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.entity.DependencyProvider
import com.google.protobuf.Empty

class GetCountsHandler(private val dp: DependencyProvider) : GrpcApiHandler<Empty, BrsApi.Counts> {
    override fun handleRequest(request: Empty): BrsApi.Counts {
        var totalEffectiveBalance: Long = 0
        val numberOfBlocks = dp.blockchainService.height + 1 // Height + genesis
        val numberOfTransactions = dp.blockchainService.getTransactionCount()
        val numberOfAccounts = dp.accountService.count
        val numberOfAssets = dp.assetExchangeService.assetsCount
        val numberOfAskOrders = dp.assetExchangeService.askCount
        val numberOfBidOrders = dp.assetExchangeService.bidCount
        val numberOfOrders = numberOfAskOrders + numberOfBidOrders
        val numberOfTrades = dp.assetExchangeService.tradesCount
        val numberOfTransfers = dp.assetExchangeService.assetTransferCount
        val numberOfAliases = dp.aliasService.getAliasCount()
        val numberOfPeers = dp.peerService.allPeers.size
        val numberOfGenerators = dp.generatorService.numberOfGenerators
        for (account in dp.accountService.getAllAccounts(0, -1)) {
            val effectiveBalanceBURST = account.balancePlanck
            if (effectiveBalanceBURST > 0) {
                totalEffectiveBalance += effectiveBalanceBURST
            }
        }
        for (escrow in dp.escrowService.getAllEscrowTransactions()) {
            totalEffectiveBalance += escrow.amountPlanck
        }

        return BrsApi.Counts.newBuilder()
            .setNumberOfBlocks(numberOfBlocks)
            .setNumberOfTransactions(numberOfTransactions)
            .setNumberOfAccounts(numberOfAccounts)
            .setNumberOfAssets(numberOfAssets)
            .setNumberOfOrders(numberOfOrders)
            .setNumberOfAskOrders(numberOfAskOrders)
            .setNumberOfBidOrders(numberOfBidOrders)
            .setNumberOfTrades(numberOfTrades)
            .setNumberOfTransfers(numberOfTransfers)
            .setNumberOfAliases(numberOfAliases)
            .setNumberOfPeers(numberOfPeers)
            .setNumberOfGenerators(numberOfGenerators)
            .setTotalEffectiveBalance(totalEffectiveBalance)
            .build()
    }
}
