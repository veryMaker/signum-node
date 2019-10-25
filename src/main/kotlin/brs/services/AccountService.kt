package brs.services

import brs.entity.AssetTransfer
import brs.entity.Account
import brs.entity.Account.*
import brs.util.Observable

interface AccountService : Observable<Account, Event> {
    fun getCount(): Int

    fun addAssetListener(eventType: Event, listener: (AccountAsset) -> Unit)

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
    fun addToForgedBalancePlanck(account: Account, amountPlanck: Long)

    fun setAccountInfo(account: Account, name: String, description: String)

    fun addToAssetBalanceQuantity(account: Account, assetId: Long, quantity: Long)

    fun addToUnconfirmedAssetBalanceQuantity(account: Account, assetId: Long, quantity: Long)

    fun addToAssetAndUnconfirmedAssetBalanceQuantity(account: Account, assetId: Long, quantity: Long)

    fun addToBalancePlanck(account: Account, amountPlanck: Long)

    fun addToUnconfirmedBalancePlanck(account: Account, amountPlanck: Long)

    fun addToBalanceAndUnconfirmedBalancePlanck(account: Account, amountPlanck: Long)

    fun getRewardRecipientAssignment(account: Account): RewardRecipientAssignment?

    fun setRewardRecipientAssignment(account: Account, recipient: Long)

    fun getUnconfirmedAssetBalanceQuantity(account: Account, assetId: Long): Long
}
