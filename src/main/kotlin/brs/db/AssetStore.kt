package brs.db

import brs.db.BurstKey
import brs.db.EntityTable
import brs.entity.Asset

interface AssetStore {
    val assetDbKeyFactory: BurstKey.LongKeyFactory<Asset>

    val assetTable: EntityTable<Asset>

    fun getAssetsIssuedBy(accountId: Long, from: Int, to: Int): Collection<Asset>
}
