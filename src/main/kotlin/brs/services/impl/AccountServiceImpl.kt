package brs.services.impl

import brs.db.AccountStore
import brs.db.AssetTransferStore
import brs.db.BurstKey.LinkKeyFactory
import brs.db.BurstKey.LongKeyFactory
import brs.db.VersionedBatchEntityTable
import brs.db.VersionedEntityTable
import brs.entity.Account
import brs.entity.Account.*
import brs.entity.AssetTransfer
import brs.entity.DependencyProvider
import brs.objects.Constants
import brs.schema.Tables.ACCOUNT
import brs.services.AccountService
import brs.util.Listeners
import brs.util.convert.fullHashToId
import brs.util.convert.safeAdd
import brs.util.convert.toHexString
import brs.util.convert.toUnsignedString
import brs.util.crypto.Crypto
import java.util.*

class AccountServiceImpl(private val dp: DependencyProvider) : AccountService {
    private val accountStore: AccountStore
    private val accountTable: VersionedBatchEntityTable<Account>
    private val accountBurstKeyFactory: LongKeyFactory<Account>
    private val accountAssetTable: VersionedEntityTable<AccountAsset>
    private val accountAssetKeyFactory: LinkKeyFactory<AccountAsset>
    private val rewardRecipientAssignmentTable: VersionedEntityTable<RewardRecipientAssignment>
    private val rewardRecipientAssignmentKeyFactory: LongKeyFactory<RewardRecipientAssignment>

    private val assetTransferStore: AssetTransferStore

    private val listeners = Listeners<Account, Event>()
    private val assetListeners = Listeners<AccountAsset, Event>()

    override val count get() = accountTable.count

    init { // TODO don't hold references to dependency instances
        val accountStore = dp.accountStore
        this.accountStore = accountStore
        this.accountTable = accountStore.accountTable
        this.accountBurstKeyFactory = accountStore.accountKeyFactory
        this.assetTransferStore = dp.assetTransferStore
        this.accountAssetTable = accountStore.accountAssetTable
        this.accountAssetKeyFactory = accountStore.accountAssetKeyFactory
        this.rewardRecipientAssignmentTable = accountStore.rewardRecipientAssignmentTable
        this.rewardRecipientAssignmentKeyFactory = accountStore.rewardRecipientAssignmentKeyFactory
    }

    override fun addListener(eventType: Event, listener: (Account) -> Unit) {
        listeners.addListener(eventType, listener)
    }

    override fun addAssetListener(
        eventType: Event,
        listener: (AccountAsset) -> Unit
    ) {
        assetListeners.addListener(eventType, listener)
    }

    override fun getAccount(id: Long): Account? {
        return if (id == 0L) null else accountTable[accountBurstKeyFactory.newKey(id)]
    }

    override fun getAccount(id: Long, height: Int): Account? {
        return if (id == 0L) null else accountTable[accountBurstKeyFactory.newKey(id), height]
    }

    override fun getAccount(publicKey: ByteArray): Account? {
        val account = accountTable[accountBurstKeyFactory.newKey(getId(publicKey))] ?: return null

        if (account.publicKey == null || Arrays.equals(account.publicKey, publicKey)) {
            return account
        }

        throw RuntimeException("DUPLICATE KEY for account " + account.id.toUnsignedString() + " existing key " + account.publicKey.toHexString() + " new key " + publicKey.toHexString())
    }

    override fun getAssetTransfers(accountId: Long, from: Int, to: Int): Collection<AssetTransfer> {
        return assetTransferStore.getAccountAssetTransfers(accountId, from, to)
    }

    override fun getAssets(accountId: Long, from: Int, to: Int): Collection<AccountAsset> {
        return accountStore.getAssets(from, to, accountId)
    }

    override fun getAccountsWithRewardRecipient(recipientId: Long?): Collection<RewardRecipientAssignment> {
        return accountStore.getAccountsWithRewardRecipient(recipientId)
    }

    override fun getAccountsWithName(name: String): Collection<Account> {
        return accountTable.getManyBy(ACCOUNT.NAME.equalIgnoreCase(name), 0, -1)
    }

    override fun getAllAccounts(from: Int, to: Int): Collection<Account> {
        return accountTable.getAll(from, to)
    }

    override fun getOrAddAccount(id: Long): Account {
        var account: Account? = accountTable[accountBurstKeyFactory.newKey(id)]
        if (account == null) {
            account = Account(dp, id)
            accountTable.insert(account)
        }
        return account
    }

    override fun flushAccountTable() {
        accountTable.finish()
    }

    override fun addToForgedBalancePlanck(account: Account, amountPlanck: Long) {
        if (amountPlanck == 0L) {
            return
        }
        account.forgedBalancePlanck = account.forgedBalancePlanck.safeAdd(amountPlanck)
        accountTable.insert(account)
    }

    override fun setAccountInfo(account: Account, name: String, description: String) {
        account.name = name.trim { it <= ' ' }
        account.description = description.trim { it <= ' ' }
        accountTable.insert(account)
    }

    override fun addToAssetBalanceQuantity(account: Account, assetId: Long, quantity: Long) {
        if (quantity == 0L) {
            return
        }
        var accountAsset: AccountAsset?

        val newKey = accountAssetKeyFactory.newKey(account.id, assetId)
        accountAsset = accountAssetTable[newKey]
        var assetBalance = accountAsset?.quantity ?: 0
        assetBalance = assetBalance.safeAdd(quantity)
        if (accountAsset == null) {
            accountAsset = AccountAsset(newKey, account.id, assetId, assetBalance, 0)
        } else {
            accountAsset.quantity = assetBalance
        }
        saveAccountAsset(accountAsset)
        listeners.accept(Event.ASSET_BALANCE, account)
        assetListeners.accept(Event.ASSET_BALANCE, accountAsset)
    }

    override fun addToUnconfirmedAssetBalanceQuantity(account: Account, assetId: Long, quantity: Long) {
        if (quantity == 0L) {
            return
        }
        var accountAsset: AccountAsset?
        val newKey = accountAssetKeyFactory.newKey(account.id, assetId)
        accountAsset = accountAssetTable[newKey]
        var unconfirmedAssetBalance = accountAsset?.unconfirmedQuantity ?: 0
        unconfirmedAssetBalance = unconfirmedAssetBalance.safeAdd(quantity)
        if (accountAsset == null) {
            accountAsset = AccountAsset(newKey, account.id, assetId, 0, unconfirmedAssetBalance)
        } else {
            accountAsset.unconfirmedQuantity = unconfirmedAssetBalance
        }
        saveAccountAsset(accountAsset)
        listeners.accept(Event.UNCONFIRMED_ASSET_BALANCE, account)
        assetListeners.accept(Event.UNCONFIRMED_ASSET_BALANCE, accountAsset)
    }

    override fun addToAssetAndUnconfirmedAssetBalanceQuantity(account: Account, assetId: Long, quantity: Long) {
        if (quantity == 0L) {
            return
        }
        var accountAsset: AccountAsset?
        val newKey = accountAssetKeyFactory.newKey(account.id, assetId)
        accountAsset = accountAssetTable[newKey]
        var assetBalance = accountAsset?.quantity ?: 0
        assetBalance = assetBalance.safeAdd(quantity)
        var unconfirmedAssetBalance = accountAsset?.unconfirmedQuantity ?: 0
        unconfirmedAssetBalance = unconfirmedAssetBalance.safeAdd(quantity)
        if (accountAsset == null) {
            accountAsset = AccountAsset(newKey, account.id, assetId, assetBalance, unconfirmedAssetBalance)
        } else {
            accountAsset.quantity = assetBalance
            accountAsset.unconfirmedQuantity = unconfirmedAssetBalance
        }
        saveAccountAsset(accountAsset)
        listeners.accept(Event.ASSET_BALANCE, account)
        listeners.accept(Event.UNCONFIRMED_ASSET_BALANCE, account)
        assetListeners.accept(Event.ASSET_BALANCE, accountAsset)
        assetListeners.accept(Event.UNCONFIRMED_ASSET_BALANCE, accountAsset)
    }

    override fun addToBalancePlanck(account: Account, amountPlanck: Long) {
        if (amountPlanck == 0L) {
            return
        }
        account.balancePlanck = account.balancePlanck.safeAdd(amountPlanck)
        account.checkBalance()
        accountTable.insert(account)
        listeners.accept(Event.BALANCE, account)
    }

    override fun addToUnconfirmedBalancePlanck(account: Account, amountPlanck: Long) {
        if (amountPlanck == 0L) {
            return
        }
        account.unconfirmedBalancePlanck = account.unconfirmedBalancePlanck.safeAdd(amountPlanck)
        account.checkBalance()
        accountTable.insert(account)
        listeners.accept(Event.UNCONFIRMED_BALANCE, account)
    }

    override fun addToBalanceAndUnconfirmedBalancePlanck(account: Account, amountPlanck: Long) {
        if (amountPlanck == 0L) {
            return
        }
        account.balancePlanck = account.balancePlanck.safeAdd(amountPlanck)
        account.unconfirmedBalancePlanck = account.unconfirmedBalancePlanck.safeAdd(amountPlanck)
        account.checkBalance()
        accountTable.insert(account)
        listeners.accept(Event.BALANCE, account)
        listeners.accept(Event.UNCONFIRMED_BALANCE, account)
    }

    override fun getRewardRecipientAssignment(account: Account): RewardRecipientAssignment? {
        return rewardRecipientAssignmentTable[rewardRecipientAssignmentKeyFactory.newKey(account.id)]
    }

    override fun setRewardRecipientAssignment(account: Account, recipient: Long) {
        val currentHeight = dp.blockchainService.height
        var assignment = getRewardRecipientAssignment(account)
        if (assignment == null) {
            val burstKey = rewardRecipientAssignmentKeyFactory.newKey(account.id)
            assignment = RewardRecipientAssignment(
                account.id,
                account.id,
                recipient,
                (currentHeight + Constants.BURST_REWARD_RECIPIENT_ASSIGNMENT_WAIT_TIME).toInt(),
                burstKey
            )
        } else {
            assignment.setRecipient(
                recipient,
                (currentHeight + Constants.BURST_REWARD_RECIPIENT_ASSIGNMENT_WAIT_TIME).toInt()
            )
        }
        rewardRecipientAssignmentTable.insert(assignment)
    }

    override fun getUnconfirmedAssetBalanceQuantity(account: Account, assetId: Long): Long {
        val newKey = accountAssetKeyFactory.newKey(account.id, assetId)
        val accountAsset = accountAssetTable[newKey]
        return accountAsset?.unconfirmedQuantity ?: 0
    }


    private fun saveAccountAsset(accountAsset: AccountAsset) {
        accountAsset.checkBalance()
        if (accountAsset.quantity > 0 || accountAsset.unconfirmedQuantity > 0) {
            accountAssetTable.insert(accountAsset)
        } else {
            accountAssetTable.delete(accountAsset)
        }
    }

    companion object {

        fun getId(publicKey: ByteArray): Long {
            val publicKeyHash = Crypto.sha256().digest(publicKey)
            return publicKeyHash.fullHashToId()
        }
    }
}
