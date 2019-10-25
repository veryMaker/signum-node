package brs.assetexchange

import brs.*
import brs.Account.AccountAsset
import brs.db.store.AssetStore

internal class AssetServiceImpl(private val assetAccountService: AssetAccountServiceImpl, private val tradeService: TradeServiceImpl, private val assetStore: AssetStore, private val assetTransferService: AssetTransferServiceImpl) { // TODO interface
    private val assetTable = assetStore.assetTable
    private val assetDbKeyFactory = assetStore.assetDbKeyFactory

    val assetsCount get() = assetTable.count

    fun getAsset(id: Long): Asset? {
        return assetTable[assetDbKeyFactory.newKey(id)]
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
