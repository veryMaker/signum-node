package brs.assetexchange

import brs.Account.AccountAsset
import brs.*
import brs.db.BurstKey
import brs.db.sql.EntitySqlTable
import brs.db.store.AssetStore

internal class AssetServiceImpl(private val assetAccountService: AssetAccountServiceImpl, private val tradeService: TradeServiceImpl, private val assetStore: AssetStore, private val assetTransferService: AssetTransferServiceImpl) {

    private val assetTable: EntitySqlTable<Asset>

    private val assetDbKeyFactory: BurstKey.LongKeyFactory<Asset>

    val assetsCount: Int
        get() = assetTable.count

    init {
        this.assetTable = assetStore.assetTable
        this.assetDbKeyFactory = assetStore.assetDbKeyFactory
    }

    fun getAsset(id: Long): Asset {
        return assetTable.get(assetDbKeyFactory.newKey(id))
    }

    fun getAccounts(assetId: Long, from: Int, to: Int): Collection<AccountAsset> {
        return assetAccountService.getAssetAccounts(assetId, from, to)
    }

    fun getAccounts(assetId: Long, height: Int, from: Int, to: Int): Collection<AccountAsset> {
        return if (height < 0) {
            getAccounts(assetId, from, to)
        } else assetAccountService.getAssetAccounts(assetId, height, from, to)
    }

    fun getTrades(assetId: Long, from: Int, to: Int): Collection<Trade> {
        return tradeService.getAssetTrades(assetId, from, to)
    }

    fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer> {
        return assetTransferService.getAssetTransfers(assetId, from, to)
    }

    fun getAllAssets(from: Int, to: Int): Collection<Asset> {
        return assetTable.getAll(from, to)
    }

    fun getAssetsIssuedBy(accountId: Long, from: Int, to: Int): Collection<Asset> {
        return assetStore.getAssetsIssuedBy(accountId, from, to)
    }

    fun addAsset(transaction: Transaction, attachment: Attachment.ColoredCoinsAssetIssuance) {
        val dbKey = assetDbKeyFactory.newKey(transaction.id)
        assetTable.insert(Asset(dbKey, transaction, attachment))
    }

}
