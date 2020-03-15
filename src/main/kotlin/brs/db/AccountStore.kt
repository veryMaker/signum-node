package brs.db

import brs.entity.Account

/**
 * Interface for Database operations related to Accounts
 */
interface AccountStore {
    /**
     * TODO
     */
    val accountTable: BatchEntityTable<Account>

    /**
     * TODO
     */
    val rewardRecipientAssignmentTable: MutableEntityTable<Account.RewardRecipientAssignment>

    /**
     * TODO
     */
    val rewardRecipientAssignmentKeyFactory: BurstKey.LongKeyFactory<Account.RewardRecipientAssignment>

    /**
     * TODO
     */
    val accountAssetKeyFactory: BurstKey.LinkKeyFactory<Account.AccountAsset>

    /**
     * TODO
     */
    val accountAssetTable: MutableEntityTable<Account.AccountAsset>

    /**
     * TODO
     */
    val accountKeyFactory: BurstKey.LongKeyFactory<Account>

    /**
     * TODO
     */
    fun getAssetAccountsCount(assetId: Long): Int

    /**
     * TODO
     */
    fun getAccountsWithRewardRecipient(recipientId: Long?): Collection<Account.RewardRecipientAssignment>

    /**
     * TODO
     */
    fun getAssets(from: Int, to: Int, id: Long?): Collection<Account.AccountAsset>

    /**
     * TODO
     */
    fun getAssetAccounts(assetId: Long, from: Int, to: Int): Collection<Account.AccountAsset>

    /**
     * TODO
     */
    fun getAssetAccounts(assetId: Long, height: Int, from: Int, to: Int): Collection<Account.AccountAsset>

    /**
     * Set or verify an account's public key.
     * If the account's public key is null, it is set to [key] and this returns true.
     * If the account's public key is equal to [key], this returns true.
     * Otherwise this returns false.
     * @param account The account to set or verify the public key of
     * @param key The public key to use in setting or verify
     * @param height TODO
     * @return True if `account.publicKey == key` after execution (either it was set to key or was equal to before)
     */
    fun setOrVerify(account: Account, key: ByteArray, height: Int): Boolean
}
