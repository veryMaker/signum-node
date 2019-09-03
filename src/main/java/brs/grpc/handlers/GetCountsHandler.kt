package brs.grpc.handlers

import brs.Account
import brs.Blockchain
import brs.Escrow
import brs.Generator
import brs.assetexchange.AssetExchange
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.peer.Peers
import brs.services.AccountService
import brs.services.AliasService
import brs.services.EscrowService
import com.google.protobuf.Empty

class GetCountsHandler(private val accountService: AccountService, private val escrowService: EscrowService, private val blockchain: Blockchain, private val assetExchange: AssetExchange, private val aliasService: AliasService, private val generator: Generator) : GrpcApiHandler<Empty, BrsApi.Counts> {

    @Throws(Exception::class)
    override fun handleRequest(empty: Empty): BrsApi.Counts {
        var totalEffectiveBalance: Long = 0
        val numberOfBlocks = blockchain.height + 1 // Height + genesis
        val numberOfTransactions = blockchain.transactionCount
        val numberOfAccounts = accountService.count
        val numberOfAssets = assetExchange.assetsCount
        val numberOfAskOrders = assetExchange.askCount
        val numberOfBidOrders = assetExchange.bidCount
        val numberOfOrders = numberOfAskOrders + numberOfBidOrders
        val numberOfTrades = assetExchange.tradesCount
        val numberOfTransfers = assetExchange.assetTransferCount
        val numberOfAliases = aliasService.aliasCount
        val numberOfPeers = Peers.allPeers.size
        val numberOfGenerators = generator.allGenerators.size
        for (account in accountService.getAllAccounts(0, -1)) {
            val effectiveBalanceBURST = account.balanceNQT
            if (effectiveBalanceBURST > 0) {
                totalEffectiveBalance += effectiveBalanceBURST
            }
        }
        for (escrow in escrowService.allEscrowTransactions) {
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
