package brs.db.store

import brs.Account
import brs.db.BurstKey
import brs.db.VersionedBatchEntityTable
import brs.db.VersionedEntityTable

/**
 * Interface for Database operations related to Accounts
 */
interface AccountStore {

    val accountTable: VersionedBatchEntityTable<Account>

    val rewardRecipientAssignmentTable: VersionedEntityTable<Account.RewardRecipientAssignment>

    val rewardRecipientAssignmentKeyFactory: BurstKey.LongKeyFactory<Account.RewardRecipientAssignment>

    val accountAssetKeyFactory: BurstKey.LinkKeyFactory<Account.AccountAsset>

    val accountAssetTable: VersionedEntityTable<Account.AccountAsset>

    val accountKeyFactory: BurstKey.LongKeyFactory<Account>

    fun getAssetAccountsCount(assetId: Long): Int

    fun getAccountsWithRewardRecipient(recipientId: Long?): Collection<Account.RewardRecipientAssignment>

    fun getAssets(from: Int, to: Int, id: Long?): Collection<Account.AccountAsset>

    fun getAssetAccounts(assetId: Long, from: Int, to: Int): Collection<Account.AccountAsset>

    fun getAssetAccounts(assetId: Long, height: Int, from: Int, to: Int): Collection<Account.AccountAsset>
    // returns true iff:
    // this.publicKey is set to null (in which case this.publicKey also gets set to key)
    // or
    // this.publicKey is already set to an array equal to key
    fun setOrVerify(acc: Account, key: ByteArray, height: Int): Boolean
}
