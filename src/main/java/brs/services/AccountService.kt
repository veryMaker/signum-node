package brs.services

import brs.Account
import brs.Account.*
import brs.AssetTransfer
import brs.util.Observable

interface AccountService : Observable<Account, Event> {
    val count: Int

    suspend fun addAssetListener(eventType: Event, listener: suspend (AccountAsset) -> Unit)

    fun getAccount(id: Long): Account?

    fun getAccount(id: Long, height: Int): Account?

    fun getAccount(publicKey: ByteArray): Account?

    fun getAssetTransfers(accountId: Long, from: Int, to: Int): Collection<AssetTransfer>

    fun getAssets(accountId: Long, from: Int, to: Int): Collection<AccountAsset>

    fun getAccountsWithRewardRecipient(recipientId: Long?): Collection<RewardRecipientAssignment>

    fun getAccountsWithName(name: String): Collection<Account>

    fun getAllAccounts(from: Int, to: Int): Collection<Account>

    fun getOrAddAccount(id: Long): Account

    fun flushAccountTable()

    // TODO rename methods
    fun addToForgedBalanceNQT(account: Account, amountNQT: Long)

    fun setAccountInfo(account: Account, name: String, description: String)

    suspend fun addToAssetBalanceQNT(account: Account, assetId: Long, quantityQNT: Long)

    suspend fun addToUnconfirmedAssetBalanceQNT(account: Account, assetId: Long, quantityQNT: Long)

    suspend fun addToAssetAndUnconfirmedAssetBalanceQNT(account: Account, assetId: Long, quantityQNT: Long)

    suspend fun addToBalanceNQT(account: Account, amountNQT: Long)

    suspend fun addToUnconfirmedBalanceNQT(account: Account, amountNQT: Long)

    suspend fun addToBalanceAndUnconfirmedBalanceNQT(account: Account, amountNQT: Long)

    fun getRewardRecipientAssignment(account: Account): RewardRecipientAssignment?

    fun setRewardRecipientAssignment(account: Account, recipient: Long)

    fun getUnconfirmedAssetBalanceQNT(account: Account, assetId: Long): Long
}
