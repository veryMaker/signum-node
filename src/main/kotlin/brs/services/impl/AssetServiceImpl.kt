package brs.services.impl

import brs.entity.Asset
import brs.entity.AssetTransfer
import brs.entity.Trade
import brs.entity.Account.AccountAsset
import brs.db.store.AssetStore
import brs.services.AssetService
import brs.entity.Transaction
import brs.transaction.appendix.Attachment

internal class AssetServiceImpl(private val assetAccountService: AssetAccountServiceImpl, private val tradeService: AssetTradeServiceImpl, private val assetStore: AssetStore, private val assetTransferService: AssetTransferServiceImpl) :
    AssetService {
    override val assetTable = assetStore.assetTable
    override val assetDbKeyFactory = assetStore.assetDbKeyFactory

    override val assetsCount get() = assetTable.count

    override fun getAsset(id: Long): Asset? {
        return assetTable[assetDbKeyFactory.newKey(id)]
    }

    override fun getAccounts(assetId: Long, from: Int, to: Int): Collection<AccountAsset> {
        return assetAccountService.getAssetAccounts(assetId, from, to)
    }

    override fun getAccounts(assetId: Long, height: Int, from: Int, to: Int): Collection<AccountAsset> {
        return if (height < 0) {
            getAccounts(assetId, from, to)
        } else assetAccountService.getAssetAccounts(assetId, height, from, to)
    }

    override fun getTrades(assetId: Long, from: Int, to: Int): Collection<Trade> {
        return tradeService.getAssetTrades(assetId, from, to)
    }

    override fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer> {
        return assetTransferService.getAssetTransfers(assetId, from, to)
    }

    override fun getAllAssets(from: Int, to: Int): Collection<Asset> {
        return assetTable.getAll(from, to)
    }

    override fun getAssetsIssuedBy(accountId: Long, from: Int, to: Int): Collection<Asset> {
        return assetStore.getAssetsIssuedBy(accountId, from, to)
    }

    override fun addAsset(transaction: Transaction, attachment: Attachment.ColoredCoinsAssetIssuance) {
        val dbKey = assetDbKeyFactory.newKey(transaction.id)
        assetTable.insert(Asset(dbKey, transaction, attachment))
    }
}
