package brs.assetexchange

import brs.Account.AccountAsset
import brs.db.store.AccountStore

internal class AssetAccountServiceImpl(private val accountStore: AccountStore) { // TODO interface

    suspend fun getAssetAccounts(assetId: Long, from: Int, to: Int): Collection<AccountAsset> {
        return accountStore.getAssetAccounts(assetId, from, to)
    }

    suspend fun getAssetAccounts(assetId: Long, height: Int, from: Int, to: Int): Collection<AccountAsset> {
        return if (height < 0) {
            getAssetAccounts(assetId, from, to)
        } else accountStore.getAssetAccounts(assetId, height, from, to)
    }

    suspend fun getAssetAccountsCount(assetId: Long): Int {
        return accountStore.getAssetAccountsCount(assetId)
    }
}
