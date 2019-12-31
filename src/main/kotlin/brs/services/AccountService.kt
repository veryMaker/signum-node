package brs.services

import brs.entity.Account
import brs.entity.Account.*
import brs.entity.AssetTransfer
import brs.util.Observable

interface AccountService : Observable<Account, Event> {
    /**
     * The number of Accounts.
     */
    val count: Int

    /**
     * Add a listener that will be notified whenever this [eventType] occurs
     * @param eventType The type of [Event] to listen for
     * @param listener The function that will be called when this [eventType] occurs
     */
    fun addAssetListener(eventType: Event, listener: (AccountAsset) -> Unit)

    /**
     * Get an account using its ID
     * @param id The ID of the account
     * @return The account, or `null` if no account exists with that ID
     */
    fun getAccount(id: Long): Account?

    /**
     * TODO
     */
    fun getAccount(id: Long, height: Int): Account?

    /**
     * Get an account using its public key
     * @param publicKey The public key of the account
     * @return The account, or `null` if no account exists with that public key
     */
    fun getAccount(publicKey: ByteArray): Account?

    /**
     * Get an account's asset transfers using its ID
     * @param accountId The ID of the account
     * @param from The lower bound for limiting the number of results returned
     * @param to The upper bound for limiting the number of results returned
     * @return The asset transfers in the specified range
     */
    fun getAssetTransfers(accountId: Long, from: Int, to: Int): Collection<AssetTransfer>

    /**
     * Get an account's owned assets using its ID
     * @param accountId The ID of the account
     * @param from The lower bound for limiting the number of results returned
     * @param to The upper bound for limiting the number of results returned
     * @return The assets that that account owns in the specified range
     */
    fun getAssets(accountId: Long, from: Int, to: Int): Collection<AccountAsset>

    /**
     * Get a collection of reward recipient assignments where the reward is given to an account
     * @param recipientId The ID of the reward recipient
     * @return The assignments made to that account
     */
    fun getAccountsWithRewardRecipient(recipientId: Long?): Collection<RewardRecipientAssignment>

    /**
     * Get accounts with a specific name
     * @param name The name of the accounts
     * @return All accounts with that name
     */
    fun getAccountsWithName(name: String): Collection<Account>

    /**
     * Get all accounts
     * @param from The lower bound for limiting the number of results returned
     * @param to The upper bound for limiting the number of results returned
     * @return All accounts in the specified range
     */
    fun getAllAccounts(from: Int, to: Int): Collection<Account>

    /**
     * Get an account using its ID, or create an account with that ID if it does not exist
     * @param id The ID of the account
     * @return The account if it existed or the newly created account
     */
    fun getOrAddAccount(id: Long): Account

    /**
     * TODO
     */
    fun flushAccountTable()

    /**
     * Add to the forged balance of an account (This does not increase the balance of an account)
     * @param account The account
     * @param amountPlanck The amount, in Planck, to increase the account's forged balance by
     */
    fun addToForgedBalancePlanck(account: Account, amountPlanck: Long)

    /**
     * Sets the account info (name and description) of an account
     * @param account The account
     * @param name The new account name
     * @param description The new account description
     */
    fun setAccountInfo(account: Account, name: String, description: String)

    /**
     * TODO
     */
    fun addToAssetBalanceQuantity(account: Account, assetId: Long, quantity: Long)

    /**
     * TODO
     */
    fun addToUnconfirmedAssetBalanceQuantity(account: Account, assetId: Long, quantity: Long)

    /**
     * TODO
     */
    fun addToAssetAndUnconfirmedAssetBalanceQuantity(account: Account, assetId: Long, quantity: Long)

    /**
     * TODO
     */
    fun addToBalancePlanck(account: Account, amountPlanck: Long)

    /**
     * TODO
     */
    fun addToUnconfirmedBalancePlanck(account: Account, amountPlanck: Long)

    /**
     * TODO
     */
    fun addToBalanceAndUnconfirmedBalancePlanck(account: Account, amountPlanck: Long)

    /**
     * TODO
     */
    fun getRewardRecipientAssignment(account: Account): RewardRecipientAssignment?

    /**
     * TODO
     */
    fun setRewardRecipientAssignment(account: Account, recipient: Long)

    /**
     * TODO
     */
    fun getUnconfirmedAssetBalanceQuantity(account: Account, assetId: Long): Long
}
