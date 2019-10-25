package brs.services.impl

import brs.db.store.AccountStore
import brs.entity.Account.AccountAsset

internal class AssetAccountServiceImpl(private val accountStore: AccountStore) { // TODO interface
    fun getAssetAccounts(assetId: Long, from: Int, to: Int): Collection<AccountAsset> {
        return accountStore.getAssetAccounts(assetId, from, to)
    }

    fun getAssetAccounts(assetId: Long, height: Int, from: Int, to: Int): Collection<AccountAsset> {
        return if (height < 0) {
            getAssetAccounts(assetId, from, to)
        } else accountStore.getAssetAccounts(assetId, height, from, to)
    }

    fun getAssetAccountsCount(assetId: Long): Int {
        return accountStore.getAssetAccountsCount(assetId)
    }
}
