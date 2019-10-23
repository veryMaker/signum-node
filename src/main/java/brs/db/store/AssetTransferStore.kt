package brs.db.store

import brs.AssetTransfer
import brs.db.BurstKey
import brs.db.sql.EntitySqlTable

interface AssetTransferStore {
    val transferDbKeyFactory: BurstKey.LongKeyFactory<AssetTransfer>

    val assetTransferTable: EntitySqlTable<AssetTransfer>

    suspend fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer>

    suspend fun getAccountAssetTransfers(accountId: Long, from: Int, to: Int): Collection<AssetTransfer>

    suspend fun getAccountAssetTransfers(accountId: Long, assetId: Long, from: Int, to: Int): Collection<AssetTransfer>

    suspend fun getTransferCount(assetId: Long): Int
}
