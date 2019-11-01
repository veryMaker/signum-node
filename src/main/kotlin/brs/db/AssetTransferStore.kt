package brs.db

import brs.db.BurstKey
import brs.db.EntityTable
import brs.entity.AssetTransfer

interface AssetTransferStore {
    val transferDbKeyFactory: BurstKey.LongKeyFactory<AssetTransfer>

    val assetTransferTable: EntityTable<AssetTransfer>

    fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer>

    fun getAccountAssetTransfers(accountId: Long, from: Int, to: Int): Collection<AssetTransfer>

    fun getAccountAssetTransfers(accountId: Long, assetId: Long, from: Int, to: Int): Collection<AssetTransfer>

    fun getTransferCount(assetId: Long): Int
}
