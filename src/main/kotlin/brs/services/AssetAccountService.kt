package brs.services

import brs.entity.Account

interface AssetAccountService {
    /**
     * TODO
     */
    fun getAssetAccounts(assetId: Long, from: Int, to: Int): Collection<Account.AccountAsset>

    /**
     * TODO
     */
    fun getAssetAccounts(assetId: Long, height: Int, from: Int, to: Int): Collection<Account.AccountAsset>

    /**
     * TODO
     */
    fun getAssetAccountsCount(assetId: Long): Int
}