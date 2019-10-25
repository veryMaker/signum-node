package brs.services

import brs.db.BurstKey
import brs.db.EntityTable
import brs.entity.*
import brs.transaction.appendix.Attachment

interface AssetService {
    val assetTable: EntityTable<Asset>
    val assetDbKeyFactory: BurstKey.LongKeyFactory<Asset>
    val assetsCount: Int
    fun getAsset(id: Long): Asset?
    fun getAccounts(assetId: Long, from: Int, to: Int): Collection<Account.AccountAsset>
    fun getAccounts(assetId: Long, height: Int, from: Int, to: Int): Collection<Account.AccountAsset>
    fun getTrades(assetId: Long, from: Int, to: Int): Collection<Trade>
    fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer>
    fun getAllAssets(from: Int, to: Int): Collection<Asset>
    fun getAssetsIssuedBy(accountId: Long, from: Int, to: Int): Collection<Asset>
    fun addAsset(transaction: Transaction, attachment: Attachment.ColoredCoinsAssetIssuance)
}
