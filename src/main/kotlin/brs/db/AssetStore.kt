package brs.db

import brs.entity.Asset

interface AssetStore {
    /**
     * TODO
     */
    val assetDbKeyFactory: BurstKey.LongKeyFactory<Asset>

    /**
     * TODO
     */
    val assetTable: EntityTable<Asset>

    /**
     * TODO
     */
    fun getAssetsIssuedBy(accountId: Long, from: Int, to: Int): Collection<Asset>
}
