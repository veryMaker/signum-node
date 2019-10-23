package brs.services

import brs.Account
import brs.Account.*
import brs.AssetTransfer
import brs.util.Observable

interface AccountService : Observable<Account, Event> {
    suspend fun getCount(): Int

    suspend fun addAssetListener(eventType: Event, listener: suspend (AccountAsset) -> Unit)

    suspend fun getAccount(id: Long): Account?

    suspend fun getAccount(id: Long, height: Int): Account?

    suspend fun getAccount(publicKey: ByteArray): Account?

    suspend fun getAssetTransfers(accountId: Long, from: Int, to: Int): Collection<AssetTransfer>

    suspend fun getAssets(accountId: Long, from: Int, to: Int): Collection<AccountAsset>

    suspend fun getAccountsWithRewardRecipient(recipientId: Long?): Collection<RewardRecipientAssignment>

    suspend fun getAccountsWithName(name: String): Collection<Account>

    suspend fun getAllAccounts(from: Int, to: Int): Collection<Account>

    suspend fun getOrAddAccount(id: Long): Account

    suspend fun flushAccountTable()

    // TODO rename methods
    suspend fun addToForgedBalanceNQT(account: Account, amountNQT: Long)

    suspend fun setAccountInfo(account: Account, name: String, description: String)

    suspend fun addToAssetBalanceQNT(account: Account, assetId: Long, quantityQNT: Long)

    suspend fun addToUnconfirmedAssetBalanceQNT(account: Account, assetId: Long, quantityQNT: Long)

    suspend fun addToAssetAndUnconfirmedAssetBalanceQNT(account: Account, assetId: Long, quantityQNT: Long)

    suspend fun addToBalanceNQT(account: Account, amountNQT: Long)

    suspend fun addToUnconfirmedBalanceNQT(account: Account, amountNQT: Long)

    suspend fun addToBalanceAndUnconfirmedBalanceNQT(account: Account, amountNQT: Long)

    suspend fun getRewardRecipientAssignment(account: Account): RewardRecipientAssignment?

    suspend fun setRewardRecipientAssignment(account: Account, recipient: Long)

    suspend fun getUnconfirmedAssetBalanceQNT(account: Account, assetId: Long): Long
}
