package brs.db.store

import brs.AssetTransfer
import brs.db.BurstKey
import brs.db.sql.EntitySqlTable

interface AssetTransferStore {
    val transferDbKeyFactory: BurstKey.LongKeyFactory<AssetTransfer>

    val assetTransferTable: EntitySqlTable<AssetTransfer>

    fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer>

    fun getAccountAssetTransfers(accountId: Long, from: Int, to: Int): Collection<AssetTransfer>

    fun getAccountAssetTransfers(accountId: Long, assetId: Long, from: Int, to: Int): Collection<AssetTransfer>

    fun getTransferCount(assetId: Long): Int
}
