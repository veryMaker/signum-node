package brs.grpc.handlers

import brs.Asset
import brs.AssetTransfer
import brs.assetexchange.AssetExchange
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.AccountService

class GetAssetTransfersHandler(private val assetExchange: AssetExchange, private val accountService: AccountService) : GrpcApiHandler<BrsApi.GetAssetTransfersRequest, BrsApi.AssetTransfers> {

    @Throws(Exception::class)
    override fun handleRequest(request: BrsApi.GetAssetTransfersRequest): BrsApi.AssetTransfers {
        val accountId = request.account
        val assetId = request.asset
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex
        val transfers: Collection<AssetTransfer>
        val asset = assetExchange.getAsset(assetId)
        if (accountId == 0L) {
            transfers = assetExchange.getAssetTransfers(asset!!.id, firstIndex, lastIndex)
        } else if (assetId == 0L) {
            transfers = accountService.getAssetTransfers(accountId, firstIndex, lastIndex)
        } else {
            transfers = assetExchange.getAccountAssetTransfers(accountId, assetId, firstIndex, lastIndex)
        }
        val builder = BrsApi.AssetTransfers.newBuilder()
        transfers.forEach { transfer ->
            builder.addAssetTransfers(ProtoBuilder.buildTransfer(transfer, asset
                    ?: assetExchange.getAsset(transfer.assetId)))
        }
        return builder.build()
    }
}
