package brs.services

import brs.db.BurstKey
import brs.db.EntityTable
import brs.entity.*
import brs.transaction.appendix.Attachment

interface AssetService {
    /**
     * TODO
     */
    val assetTable: EntityTable<Asset>

    /**
     * TODO
     */
    val assetDbKeyFactory: BurstKey.LongKeyFactory<Asset>

    /**
     * TODO
     */
    val assetsCount: Int

    /**
     * TODO
     */
    fun getAsset(id: Long): Asset?

    /**
     * TODO
     */
    fun getAccounts(assetId: Long, from: Int, to: Int): Collection<Account.AccountAsset>

    /**
     * TODO
     */
    fun getAccounts(assetId: Long, height: Int, from: Int, to: Int): Collection<Account.AccountAsset>

    /**
     * TODO
     */
    fun getTrades(assetId: Long, from: Int, to: Int): Collection<Trade>

    /**
     * TODO
     */
    fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer>

    /**
     * TODO
     */
    fun getAllAssets(from: Int, to: Int): Collection<Asset>

    /**
     * TODO
     */
    fun getAssetsIssuedBy(accountId: Long, from: Int, to: Int): Collection<Asset>

    /**
     * TODO
     */
    fun addAsset(transaction: Transaction, attachment: Attachment.ColoredCoinsAssetIssuance)
}
