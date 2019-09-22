package brs.grpc.handlers

import brs.DependencyProvider
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import com.google.protobuf.Empty

class GetCountsHandler(private val dp: DependencyProvider) : GrpcApiHandler<Empty, BrsApi.Counts> {

    @Throws(Exception::class)
    override fun handleRequest(empty: Empty): BrsApi.Counts {
        var totalEffectiveBalance: Long = 0
        val numberOfBlocks = dp.blockchain.height + 1 // Height + genesis
        val numberOfTransactions = dp.blockchain.transactionCount
        val numberOfAccounts = dp.accountService.count
        val numberOfAssets = dp.assetExchange.assetsCount
        val numberOfAskOrders = dp.assetExchange.askCount
        val numberOfBidOrders = dp.assetExchange.bidCount
        val numberOfOrders = numberOfAskOrders + numberOfBidOrders
        val numberOfTrades = dp.assetExchange.tradesCount
        val numberOfTransfers = dp.assetExchange.assetTransferCount
        val numberOfAliases = dp.aliasService.aliasCount
        val numberOfPeers = dp.peers.allPeers.size
        val numberOfGenerators = dp.generator.allGenerators.size
        for (account in dp.accountService.getAllAccounts(0, -1)) {
            val effectiveBalanceBURST = account.balanceNQT
            if (effectiveBalanceBURST > 0) {
                totalEffectiveBalance += effectiveBalanceBURST
            }
        }
        for (escrow in dp.escrowService.allEscrowTransactions) {
            totalEffectiveBalance += escrow.amountNQT!!
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
