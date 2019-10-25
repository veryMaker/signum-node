package brs.db.sql

import brs.entity.Account
import brs.DependencyProvider
import brs.db.VersionedBatchEntityTable
import brs.db.VersionedEntityTable
import brs.db.store.AccountStore
import brs.schema.Tables.*
import brs.schema.tables.records.AccountAssetRecord
import brs.schema.tables.records.AccountRecord
import brs.schema.tables.records.RewardRecipAssignRecord
import brs.util.convert.toUnsignedString
import brs.util.logging.safeInfo
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SortField
import org.slf4j.LoggerFactory
import java.util.*

class SqlAccountStore(private val dp: DependencyProvider) : AccountStore {

    override val accountAssetTable: VersionedEntityTable<Account.AccountAsset>

    override val rewardRecipientAssignmentTable: VersionedEntityTable<Account.RewardRecipientAssignment>

    override val accountTable: VersionedBatchEntityTable<Account>

    override val rewardRecipientAssignmentKeyFactory: DbKey.LongKeyFactory<Account.RewardRecipientAssignment>
        get() = rewardRecipientAssignmentDbKeyFactory

    override val accountAssetKeyFactory: DbKey.LinkKeyFactory<Account.AccountAsset>
        get() = accountAssetDbKeyFactory

    override val accountKeyFactory: DbKey.LongKeyFactory<Account>
        get() = accountDbKeyFactory

    init {
        rewardRecipientAssignmentTable = object : VersionedEntitySqlTable<Account.RewardRecipientAssignment>("reward_recip_assign", REWARD_RECIP_ASSIGN, rewardRecipientAssignmentDbKeyFactory, dp) {

            override fun load(ctx: DSLContext, record: Record): Account.RewardRecipientAssignment {
                return SqlRewardRecipientAssignment(record)
            }

            override fun save(ctx: DSLContext, assignment: Account.RewardRecipientAssignment) {
                ctx.mergeInto<RewardRecipAssignRecord, Long, Long, Long, Int, Int, Boolean>(REWARD_RECIP_ASSIGN, REWARD_RECIP_ASSIGN.ACCOUNT_ID, REWARD_RECIP_ASSIGN.PREV_RECIP_ID, REWARD_RECIP_ASSIGN.RECIP_ID, REWARD_RECIP_ASSIGN.FROM_HEIGHT, REWARD_RECIP_ASSIGN.HEIGHT, REWARD_RECIP_ASSIGN.LATEST)
                        .key(REWARD_RECIP_ASSIGN.ACCOUNT_ID, REWARD_RECIP_ASSIGN.HEIGHT)
                        .values(assignment.accountId, assignment.prevRecipientId, assignment.recipientId, assignment.fromHeight, dp.blockchainService.height, true)
                        .execute()
            }
        }

        accountAssetTable = object : VersionedEntitySqlTable<Account.AccountAsset>("account_asset", ACCOUNT_ASSET, accountAssetDbKeyFactory, dp) {
            private val sort = initializeSort()

            private fun initializeSort(): List<SortField<*>> {
                return listOf<SortField<*>>(tableClass.field("quantity", Long::class.java).desc(), tableClass.field("account_id", Long::class.java).asc(), tableClass.field("asset_id", Long::class.java).asc())
            }

            override fun load(ctx: DSLContext, record: Record): Account.AccountAsset {
                return SQLAccountAsset(record)
            }

            override fun save(ctx: DSLContext, accountAsset: Account.AccountAsset) {
                ctx.mergeInto<AccountAssetRecord, Long, Long, Long, Long, Int, Boolean>(ACCOUNT_ASSET, ACCOUNT_ASSET.ACCOUNT_ID, ACCOUNT_ASSET.ASSET_ID, ACCOUNT_ASSET.QUANTITY, ACCOUNT_ASSET.UNCONFIRMED_QUANTITY, ACCOUNT_ASSET.HEIGHT, ACCOUNT_ASSET.LATEST)
                        .key(ACCOUNT_ASSET.ACCOUNT_ID, ACCOUNT_ASSET.ASSET_ID, ACCOUNT_ASSET.HEIGHT)
                        .values(accountAsset.accountId, accountAsset.assetId, accountAsset.quantity, accountAsset.unconfirmedQuantity, dp.blockchainService.height, true)
                        .execute()
            }

            override fun defaultSort(): Collection<SortField<*>> {
                return sort
            }
        }

        accountTable = object : VersionedBatchEntitySqlTable<Account>("account", ACCOUNT, accountDbKeyFactory, Account::class.java, dp) {
            override fun load(ctx: DSLContext, record: Record): Account {
                return SqlAccount(record)
            }

            override fun bulkInsert(ctx: DSLContext, accounts: Collection<Account>) {
                val height = dp.blockchainService.height
                ctx.batch(accounts.map { account ->
                        ctx.mergeInto<AccountRecord, Long, Int, Int, ByteArray, Int, Long, Long, Long, String, String, Boolean>(ACCOUNT, ACCOUNT.ID, ACCOUNT.HEIGHT, ACCOUNT.CREATION_HEIGHT, ACCOUNT.PUBLIC_KEY, ACCOUNT.KEY_HEIGHT, ACCOUNT.BALANCE,
                            ACCOUNT.UNCONFIRMED_BALANCE, ACCOUNT.FORGED_BALANCE, ACCOUNT.NAME, ACCOUNT.DESCRIPTION, ACCOUNT.LATEST)
                            .key(ACCOUNT.ID, ACCOUNT.HEIGHT)
                            .values(account.id, height, account.creationHeight, account.publicKey, account.keyHeight,
                                account.balancePlanck, account.unconfirmedBalancePlanck, account.forgedBalancePlanck, account.name, account.description, true) })
                    .execute()
            }
        }
    }

    override fun getAssetAccountsCount(assetId: Long): Int {
        return dp.db.getUsingDslContext<Int> { ctx -> ctx.selectCount().from(ACCOUNT_ASSET).where(ACCOUNT_ASSET.ASSET_ID.eq(assetId)).and(ACCOUNT_ASSET.LATEST.isTrue).fetchOne(0, Int::class.javaPrimitiveType) }
    }

    override fun getAccountsWithRewardRecipient(recipientId: Long?): Collection<Account.RewardRecipientAssignment> {
        return rewardRecipientAssignmentTable.getManyBy(getAccountsWithRewardRecipientClause(recipientId!!, dp.blockchainService.height + 1), 0, -1)
    }

    override fun getAssets(from: Int, to: Int, id: Long?): Collection<Account.AccountAsset> {
        return accountAssetTable.getManyBy(ACCOUNT_ASSET.ACCOUNT_ID.eq(id), from, to)
    }

    override fun getAssetAccounts(assetId: Long, from: Int, to: Int): Collection<Account.AccountAsset> {
        val sort = listOf(ACCOUNT_ASSET.field("quantity", Long::class.java).desc(), ACCOUNT_ASSET.field("account_id", Long::class.java).asc())
        return accountAssetTable.getManyBy(ACCOUNT_ASSET.ASSET_ID.eq(assetId), from, to, sort)
    }

    override fun getAssetAccounts(assetId: Long, height: Int, from: Int, to: Int): Collection<Account.AccountAsset> {
        if (height < 0) {
            return getAssetAccounts(assetId, from, to)
        }

        val sort = listOf<SortField<*>>(ACCOUNT_ASSET.field("quantity", Long::class.java).desc(), ACCOUNT_ASSET.field("account_id", Long::class.java).asc())
        return accountAssetTable.getManyBy(ACCOUNT_ASSET.ASSET_ID.eq(assetId), height, from, to, sort)
    }

    override fun setOrVerify(acc: Account, key: ByteArray, height: Int): Boolean {
        return when {
            acc.publicKey == null -> {
                if (dp.db.isInTransaction()) {
                    acc.publicKey = key
                    acc.keyHeight = -1
                    accountTable.insert(acc)
                }
                true
            }
            Arrays.equals(acc.publicKey, key) -> return true
            acc.keyHeight == -1 -> {
                logger.safeInfo { "DUPLICATE KEY!!!" }
                logger.safeInfo { "Account key for ${acc.id.toUnsignedString()} was already set to a different one at the same height, current height is $height, rejecting new key" }
                false
            }
            acc.keyHeight >= height -> {
                logger.safeInfo { "DUPLICATE KEY!!!" }
                if (dp.db.isInTransaction()) {
                    logger.safeInfo { "Changing key for account ${acc.id.toUnsignedString()} at height $height, was previously set to a different one at height ${acc.keyHeight}" }
                    acc.publicKey = key
                    acc.keyHeight = height
                    accountTable.insert(acc)
                }
                true
            }
            else -> {
                logger.safeInfo { "DUPLICATE KEY!!!" }
                logger.safeInfo { "Invalid key for account ${acc.id.toUnsignedString()} at height $height, was already set to a different one at height ${acc.keyHeight}" }
                false
            }
        }
    }

    internal class SQLAccountAsset(rs: Record) : Account.AccountAsset(rs.get(ACCOUNT_ASSET.ACCOUNT_ID), rs.get(ACCOUNT_ASSET.ASSET_ID), rs.get(ACCOUNT_ASSET.QUANTITY), rs.get(ACCOUNT_ASSET.UNCONFIRMED_QUANTITY), accountAssetDbKeyFactory.newKey(rs.get(ACCOUNT_ASSET.ACCOUNT_ID), rs.get(ACCOUNT_ASSET.ASSET_ID)))

    internal inner class SqlAccount(record: Record) : Account(record.get(ACCOUNT.ID), accountDbKeyFactory.newKey(record.get(ACCOUNT.ID)), record.get(ACCOUNT.CREATION_HEIGHT)) {
        init {
            this.publicKey = record.get(ACCOUNT.PUBLIC_KEY)
            this.keyHeight = record.get(ACCOUNT.KEY_HEIGHT)
            this.balancePlanck = record.get(ACCOUNT.BALANCE)
            this.unconfirmedBalancePlanck = record.get(ACCOUNT.UNCONFIRMED_BALANCE)
            this.forgedBalancePlanck = record.get(ACCOUNT.FORGED_BALANCE)
            this.name = record.get(ACCOUNT.NAME)
            this.description = record.get(ACCOUNT.DESCRIPTION)
        }
    }

    internal inner class SqlRewardRecipientAssignment(record: Record) : Account.RewardRecipientAssignment(record.get(REWARD_RECIP_ASSIGN.ACCOUNT_ID), record.get(REWARD_RECIP_ASSIGN.PREV_RECIP_ID), record.get(REWARD_RECIP_ASSIGN.RECIP_ID), record.get(REWARD_RECIP_ASSIGN.FROM_HEIGHT), rewardRecipientAssignmentDbKeyFactory.newKey(record.get(REWARD_RECIP_ASSIGN.ACCOUNT_ID)))

    companion object {

        private val accountDbKeyFactory = object : DbKey.LongKeyFactory<Account>(ACCOUNT.ID) {
            override fun newKey(account: Account): DbKey {
                return account.nxtKey as DbKey
            }
        }
        private val rewardRecipientAssignmentDbKeyFactory = object : DbKey.LongKeyFactory<Account.RewardRecipientAssignment>(REWARD_RECIP_ASSIGN.ACCOUNT_ID) {
            override fun newKey(assignment: Account.RewardRecipientAssignment): DbKey {
                return assignment.burstKey as DbKey
            }
        }
        private val logger = LoggerFactory.getLogger(SqlAccountStore::class.java)
        private val accountAssetDbKeyFactory = object : DbKey.LinkKeyFactory<Account.AccountAsset>("account_id", "asset_id") {
            override fun newKey(accountAsset: Account.AccountAsset): DbKey {
                return accountAsset.burstKey as DbKey
            }
        }

        private fun getAccountsWithRewardRecipientClause(id: Long, height: Int): Condition {
            return REWARD_RECIP_ASSIGN.RECIP_ID.eq(id).and(REWARD_RECIP_ASSIGN.FROM_HEIGHT.le(height))
        }
    }
}
