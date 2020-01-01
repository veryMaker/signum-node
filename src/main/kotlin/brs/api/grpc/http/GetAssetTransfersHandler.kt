package brs.api.grpc.http

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.ApiException
import brs.api.grpc.ProtoBuilder
import brs.entity.AssetTransfer
import brs.services.AccountService
import brs.services.AssetExchangeService

class GetAssetTransfersHandler(
    private val assetExchangeService: AssetExchangeService,
    private val accountService: AccountService
) : GrpcApiHandler<BrsApi.GetAssetTransfersRequest, BrsApi.AssetTransfers> {
    override fun handleRequest(request: BrsApi.GetAssetTransfersRequest): BrsApi.AssetTransfers {
        val accountId = request.account
        val assetId = request.asset
        val indexRange = ProtoBuilder.sanitizeIndexRange(request.indexRange)
        val firstIndex = indexRange.firstIndex
        val lastIndex = indexRange.lastIndex
        val transfers: Collection<AssetTransfer>
        val asset = assetExchangeService.getAsset(assetId)
        transfers = when {
            accountId == 0L -> assetExchangeService.getAssetTransfers(asset!!.id, firstIndex, lastIndex)
            assetId == 0L -> accountService.getAssetTransfers(accountId, firstIndex, lastIndex)
            else -> assetExchangeService.getAccountAssetTransfers(accountId, assetId, firstIndex, lastIndex)
        }
        val builder = BrsApi.AssetTransfers.newBuilder()
        transfers.forEach { transfer ->
            builder.addAssetTransfers(
                ProtoBuilder.buildTransfer(
                    transfer, asset ?: assetExchangeService.getAsset(transfer.assetId) ?: throw ApiException(
                        "Asset not found"
                    )
                )
            )
        }
        return builder.build()
    }
}
