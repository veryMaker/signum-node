package brs.services.impl

import brs.db.AccountStore
import brs.entity.Account.AccountAsset
import brs.services.AssetAccountService

internal class AssetAccountServiceImpl(private val accountStore: AccountStore) : AssetAccountService {
    override fun getAssetAccounts(assetId: Long, from: Int, to: Int): Collection<AccountAsset> {
        return accountStore.getAssetAccounts(assetId, from, to)
    }

    override fun getAssetAccounts(assetId: Long, height: Int, from: Int, to: Int): Collection<AccountAsset> {
        return if (height < 0) {
            getAssetAccounts(assetId, from, to)
        } else accountStore.getAssetAccounts(assetId, height, from, to)
    }

    override fun getAssetAccountsCount(assetId: Long): Int {
        return accountStore.getAssetAccountsCount(assetId)
    }
}
