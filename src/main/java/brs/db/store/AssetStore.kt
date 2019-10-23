package brs.db.store

import brs.Asset
import brs.db.BurstKey
import brs.db.sql.EntitySqlTable

interface AssetStore {
    val assetDbKeyFactory: BurstKey.LongKeyFactory<Asset>

    val assetTable: EntitySqlTable<Asset>

    suspend fun getAssetsIssuedBy(accountId: Long, from: Int, to: Int): Collection<Asset>
}
