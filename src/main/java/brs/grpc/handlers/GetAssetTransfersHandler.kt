package brs.grpc.handlers

import brs.AssetTransfer
import brs.assetexchange.AssetExchange
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.services.AccountService

class GetAssetTransfersHandler(private val assetExchange: AssetExchange, private val accountService: AccountService) : GrpcApiHandler<BrsApi.GetAssetTransfersRequest, BrsApi.AssetTransfers> {

    override fun handleRequest(request: BrsApi.GetAssetTransfersRequest): BrsApi.AssetTransfers {
        val accountId = request.account
        val assetId = request.asset
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex
        val transfers: Collection<AssetTransfer>
        val asset = assetExchange.getAsset(assetId)
        transfers = when {
            accountId == 0L -> assetExchange.getAssetTransfers(asset!!.id, firstIndex, lastIndex)
            assetId == 0L -> accountService.getAssetTransfers(accountId, firstIndex, lastIndex)
            else -> assetExchange.getAccountAssetTransfers(accountId, assetId, firstIndex, lastIndex)
        }
        val builder = BrsApi.AssetTransfers.newBuilder()
        transfers.forEach { transfer ->
            builder.addAssetTransfers(ProtoBuilder.buildTransfer(transfer, asset ?: assetExchange.getAsset(transfer.assetId) ?: throw ApiException("Asset not found")))
        }
        return builder.build()
    }
}
