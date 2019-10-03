package brs.services.impl

import brs.Account
import brs.Account.*
import brs.AssetTransfer
import brs.Constants
import brs.DependencyProvider
import brs.crypto.Crypto
import brs.db.BurstKey.LinkKeyFactory
import brs.db.BurstKey.LongKeyFactory
import brs.db.VersionedBatchEntityTable
import brs.db.VersionedEntityTable
import brs.db.store.AccountStore
import brs.db.store.AssetTransferStore
import brs.schema.Tables.ACCOUNT
import brs.services.AccountService
import brs.util.Listeners
import brs.util.convert.fullHashToId
import brs.util.convert.safeAdd
import brs.util.convert.toHexString
import brs.util.convert.toUnsignedString
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

    override val count: Int
        get() = accountTable.count

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

    override suspend fun addListener(eventType: Event, listener: suspend (Account) -> Unit) {
        listeners.addListener(eventType, listener)
    }

    override suspend fun addAssetListener(
        eventType: Event,
        listener: suspend (AccountAsset) -> Unit
    ) {
        assetListeners.addListener(eventType, listener)
    }

    override fun getAccount(id: Long): Account? {
        return if (id == 0L) null else accountTable.get(accountBurstKeyFactory.newKey(id))
    }

    override fun getAccount(id: Long, height: Int): Account? {
        return if (id == 0L) null else accountTable.get(accountBurstKeyFactory.newKey(id), height)
    }

    override fun getAccount(publicKey: ByteArray): Account? {
        val account = accountTable[accountBurstKeyFactory.newKey(getId(publicKey))] ?: return null

        if (account.publicKey == null || Arrays.equals(account.publicKey, publicKey)) {
            return account
        }

        throw RuntimeException("DUPLICATE KEY for account " + account.id.toUnsignedString() + " existing key " + account.publicKey!!.toHexString() + " new key " + publicKey.toHexString())
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
        var account: Account? = accountTable.get(accountBurstKeyFactory.newKey(id))
        if (account == null) {
            account = Account(dp, id)
            accountTable.insert(account)
        }
        return account
    }

    override fun flushAccountTable() {
        accountTable.finish()
    }

    override fun addToForgedBalanceNQT(account: Account, amountNQT: Long) {
        if (amountNQT == 0L) {
            return
        }
        account.forgedBalanceNQT = account.forgedBalanceNQT.safeAdd(amountNQT)
        accountTable.insert(account)
    }

    override fun setAccountInfo(account: Account, name: String, description: String) {
        account.name = name.trim { it <= ' ' }
        account.description = description.trim { it <= ' ' }
        accountTable.insert(account)
    }

    override suspend fun addToAssetBalanceQNT(account: Account, assetId: Long, quantityQNT: Long) {
        if (quantityQNT == 0L) {
            return
        }
        var accountAsset: AccountAsset?

        val newKey = accountAssetKeyFactory.newKey(account.id, assetId)
        accountAsset = accountAssetTable.get(newKey)
        var assetBalance = accountAsset?.quantityQNT ?: 0
        assetBalance = assetBalance.safeAdd(quantityQNT)
        if (accountAsset == null) {
            accountAsset = AccountAsset(newKey, account.id, assetId, assetBalance, 0)
        } else {
            accountAsset.quantityQNT = assetBalance
        }
        saveAccountAsset(accountAsset)
        listeners.accept(Event.ASSET_BALANCE, account)
        assetListeners.accept(Event.ASSET_BALANCE, accountAsset)
    }

    override suspend fun addToUnconfirmedAssetBalanceQNT(account: Account, assetId: Long, quantityQNT: Long) {
        if (quantityQNT == 0L) {
            return
        }
        var accountAsset: AccountAsset?
        val newKey = accountAssetKeyFactory.newKey(account.id, assetId)
        accountAsset = accountAssetTable.get(newKey)
        var unconfirmedAssetBalance = accountAsset?.unconfirmedQuantityQNT ?: 0
        unconfirmedAssetBalance = unconfirmedAssetBalance.safeAdd(quantityQNT)
        if (accountAsset == null) {
            accountAsset = AccountAsset(newKey, account.id, assetId, 0, unconfirmedAssetBalance)
        } else {
            accountAsset.unconfirmedQuantityQNT = unconfirmedAssetBalance
        }
        saveAccountAsset(accountAsset)
        listeners.accept(Event.UNCONFIRMED_ASSET_BALANCE, account)
        assetListeners.accept(Event.UNCONFIRMED_ASSET_BALANCE, accountAsset)
    }

    override suspend fun addToAssetAndUnconfirmedAssetBalanceQNT(account: Account, assetId: Long, quantityQNT: Long) {
        if (quantityQNT == 0L) {
            return
        }
        var accountAsset: AccountAsset?
        val newKey = accountAssetKeyFactory.newKey(account.id, assetId)
        accountAsset = accountAssetTable.get(newKey)
        var assetBalance = accountAsset?.quantityQNT ?: 0
        assetBalance = assetBalance.safeAdd(quantityQNT)
        var unconfirmedAssetBalance = accountAsset?.unconfirmedQuantityQNT ?: 0
        unconfirmedAssetBalance = unconfirmedAssetBalance.safeAdd(quantityQNT)
        if (accountAsset == null) {
            accountAsset = AccountAsset(newKey, account.id, assetId, assetBalance, unconfirmedAssetBalance)
        } else {
            accountAsset.quantityQNT = assetBalance
            accountAsset.unconfirmedQuantityQNT = unconfirmedAssetBalance
        }
        saveAccountAsset(accountAsset)
        listeners.accept(Event.ASSET_BALANCE, account)
        listeners.accept(Event.UNCONFIRMED_ASSET_BALANCE, account)
        assetListeners.accept(Event.ASSET_BALANCE, accountAsset)
        assetListeners.accept(Event.UNCONFIRMED_ASSET_BALANCE, accountAsset)
    }

    override suspend fun addToBalanceNQT(account: Account, amountNQT: Long) {
        if (amountNQT == 0L) {
            return
        }
        account.balanceNQT = account.balanceNQT.safeAdd(amountNQT)
        account.checkBalance()
        accountTable.insert(account)
        listeners.accept(Event.BALANCE, account)
    }

    override suspend fun addToUnconfirmedBalanceNQT(account: Account, amountNQT: Long) {
        if (amountNQT == 0L) {
            return
        }
        account.unconfirmedBalanceNQT = account.unconfirmedBalanceNQT.safeAdd(amountNQT)
        account.checkBalance()
        accountTable.insert(account)
        listeners.accept(Event.UNCONFIRMED_BALANCE, account)
    }

    override suspend fun addToBalanceAndUnconfirmedBalanceNQT(account: Account, amountNQT: Long) {
        if (amountNQT == 0L) {
            return
        }
        account.balanceNQT = account.balanceNQT.safeAdd(amountNQT)
        account.unconfirmedBalanceNQT = account.unconfirmedBalanceNQT.safeAdd(amountNQT)
        account.checkBalance()
        accountTable.insert(account)
        listeners.accept(Event.BALANCE, account)
        listeners.accept(Event.UNCONFIRMED_BALANCE, account)
    }

    override fun getRewardRecipientAssignment(account: Account): RewardRecipientAssignment? {
        return rewardRecipientAssignmentTable[rewardRecipientAssignmentKeyFactory.newKey(account.id!!)]
    }

    override fun setRewardRecipientAssignment(account: Account, recipient: Long) {
        val currentHeight = dp.blockchain.height
        var assignment: RewardRecipientAssignment? = getRewardRecipientAssignment(account)
        if (assignment == null) {
            val burstKey = rewardRecipientAssignmentKeyFactory.newKey(account.id)
            assignment = RewardRecipientAssignment(account.id, account.id, recipient, (currentHeight + Constants.BURST_REWARD_RECIPIENT_ASSIGNMENT_WAIT_TIME).toInt(), burstKey)
        } else {
            assignment.setRecipient(recipient, (currentHeight + Constants.BURST_REWARD_RECIPIENT_ASSIGNMENT_WAIT_TIME).toInt())
        }
        rewardRecipientAssignmentTable.insert(assignment)
    }

    override fun getUnconfirmedAssetBalanceQNT(account: Account, assetId: Long): Long {
        val newKey = accountAssetKeyFactory.newKey(account.id, assetId)
        val accountAsset = accountAssetTable[newKey]
        return accountAsset?.unconfirmedQuantityQNT ?: 0
    }


    private fun saveAccountAsset(accountAsset: AccountAsset) {
        accountAsset.checkBalance()
        if (accountAsset.quantityQNT > 0 || accountAsset.unconfirmedQuantityQNT > 0) {
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
