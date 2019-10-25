package brs.services

import brs.entity.Account

interface AssetAccountService {
    fun getAssetAccounts(assetId: Long, from: Int, to: Int): Collection<Account.AccountAsset>
    fun getAssetAccounts(assetId: Long, height: Int, from: Int, to: Int): Collection<Account.AccountAsset>
    fun getAssetAccountsCount(assetId: Long): Int
}