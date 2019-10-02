package brs.db.sql

import brs.Account
import brs.DependencyProvider
import brs.db.VersionedBatchEntityTable
import brs.db.VersionedEntityTable
import brs.db.store.AccountStore
import brs.schema.Tables.*
import brs.schema.tables.records.AccountAssetRecord
import brs.schema.tables.records.AccountRecord
import brs.schema.tables.records.RewardRecipAssignRecord
import brs.util.toUnsignedString
import org.jooq.*
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
        rewardRecipientAssignmentTable = object : VersionedEntitySqlTable<Account.RewardRecipientAssignment>("reward_recip_assign", brs.schema.Tables.REWARD_RECIP_ASSIGN, rewardRecipientAssignmentDbKeyFactory, dp) {

            override fun load(ctx: DSLContext, rs: Record): Account.RewardRecipientAssignment {
                return SqlRewardRecipientAssignment(rs)
            }

            override fun save(ctx: DSLContext, assignment: Account.RewardRecipientAssignment) {
                ctx.mergeInto<RewardRecipAssignRecord, Long, Long, Long, Int, Int, Boolean>(REWARD_RECIP_ASSIGN, REWARD_RECIP_ASSIGN.ACCOUNT_ID, REWARD_RECIP_ASSIGN.PREV_RECIP_ID, REWARD_RECIP_ASSIGN.RECIP_ID, REWARD_RECIP_ASSIGN.FROM_HEIGHT, REWARD_RECIP_ASSIGN.HEIGHT, REWARD_RECIP_ASSIGN.LATEST)
                        .key(REWARD_RECIP_ASSIGN.ACCOUNT_ID, REWARD_RECIP_ASSIGN.HEIGHT)
                        .values(assignment.accountId, assignment.prevRecipientId, assignment.recipientId, assignment.fromHeight, dp.blockchain.height, true)
                        .execute()
            }
        }

        accountAssetTable = object : VersionedEntitySqlTable<Account.AccountAsset>("account_asset", brs.schema.Tables.ACCOUNT_ASSET, accountAssetDbKeyFactory, dp) {
            private val sort = initializeSort()

            private fun initializeSort(): List<SortField<*>> {
                val sort = mutableListOf<SortField<*>>()
                sort.add(tableClass.field("quantity", Long::class.java).desc())
                sort.add(tableClass.field("account_id", Long::class.java).asc())
                sort.add(tableClass.field("asset_id", Long::class.java).asc())
                return sort
            }

            override fun load(ctx: DSLContext, rs: Record): Account.AccountAsset {
                return SQLAccountAsset(rs)
            }

            override fun save(ctx: DSLContext, accountAsset: Account.AccountAsset) {
                ctx.mergeInto<AccountAssetRecord, Long, Long, Long, Long, Int, Boolean>(ACCOUNT_ASSET, ACCOUNT_ASSET.ACCOUNT_ID, ACCOUNT_ASSET.ASSET_ID, ACCOUNT_ASSET.QUANTITY, ACCOUNT_ASSET.UNCONFIRMED_QUANTITY, ACCOUNT_ASSET.HEIGHT, ACCOUNT_ASSET.LATEST)
                        .key(ACCOUNT_ASSET.ACCOUNT_ID, ACCOUNT_ASSET.ASSET_ID, ACCOUNT_ASSET.HEIGHT)
                        .values(accountAsset.accountId, accountAsset.assetId, accountAsset.quantityQNT, accountAsset.unconfirmedQuantityQNT, dp.blockchain.height, true)
                        .execute()
            }

            override fun defaultSort(): List<SortField<*>> {
                return sort
            }
        }

        accountTable = object : VersionedBatchEntitySqlTable<Account>("account", brs.schema.Tables.ACCOUNT, accountDbKeyFactory, Account::class.java, dp) {
            override fun load(ctx: DSLContext, rs: Record): Account {
                return SqlAccount(rs)
            }

            override fun bulkInsert(ctx: DSLContext, accounts: Collection<Account>) {
                val accountQueries = mutableListOf<Query>()
                val height = dp.blockchain.height
                for (account in accounts) {
                    accountQueries.add(
                            ctx.mergeInto<AccountRecord, Long, Int, Int, ByteArray, Int, Long, Long, Long, String, String, Boolean>(ACCOUNT, ACCOUNT.ID, ACCOUNT.HEIGHT, ACCOUNT.CREATION_HEIGHT, ACCOUNT.PUBLIC_KEY, ACCOUNT.KEY_HEIGHT, ACCOUNT.BALANCE,
                                    ACCOUNT.UNCONFIRMED_BALANCE, ACCOUNT.FORGED_BALANCE, ACCOUNT.NAME, ACCOUNT.DESCRIPTION, ACCOUNT.LATEST)
                                    .key(ACCOUNT.ID, ACCOUNT.HEIGHT)
                                    .values(account.id, height, account.creationHeight, account.publicKey, account.keyHeight,
                                            account.balanceNQT, account.unconfirmedBalanceNQT, account.forgedBalanceNQT, account.name, account.description, true)
                    )
                }
                ctx.batch(accountQueries).execute()
            }
        }
    }

    override fun getAssetAccountsCount(assetId: Long): Int {
        return dp.db.useDslContext<Int> { ctx -> ctx.selectCount().from(ACCOUNT_ASSET).where(ACCOUNT_ASSET.ASSET_ID.eq(assetId)).and(ACCOUNT_ASSET.LATEST.isTrue).fetchOne(0, Int::class.javaPrimitiveType) }
    }

    override fun getAccountsWithRewardRecipient(recipientId: Long?): Collection<Account.RewardRecipientAssignment> {
        return rewardRecipientAssignmentTable.getManyBy(getAccountsWithRewardRecipientClause(recipientId!!, dp.blockchain.height + 1), 0, -1)
    }

    override fun getAssets(from: Int, to: Int, id: Long?): Collection<Account.AccountAsset> {
        return accountAssetTable.getManyBy(ACCOUNT_ASSET.ACCOUNT_ID.eq(id), from, to)
    }

    override fun getAssetAccounts(assetId: Long, from: Int, to: Int): Collection<Account.AccountAsset> {
        val sort = mutableListOf<SortField<*>>()
        sort.add(ACCOUNT_ASSET.field("quantity", Long::class.java).desc())
        sort.add(ACCOUNT_ASSET.field("account_id", Long::class.java).asc())
        return accountAssetTable.getManyBy(ACCOUNT_ASSET.ASSET_ID.eq(assetId), from, to, sort)
    }

    override fun getAssetAccounts(assetId: Long, height: Int, from: Int, to: Int): Collection<Account.AccountAsset> {
        if (height < 0) {
            return getAssetAccounts(assetId, from, to)
        }

        val sort = mutableListOf<SortField<*>>()
        sort.add(ACCOUNT_ASSET.field("quantity", Long::class.java).desc())
        sort.add(ACCOUNT_ASSET.field("account_id", Long::class.java).asc())
        return accountAssetTable.getManyBy(ACCOUNT_ASSET.ASSET_ID.eq(assetId), height, from, to, sort)
    }

    override fun setOrVerify(acc: Account, key: ByteArray, height: Int): Boolean {
        when {
            acc.publicKey == null -> {
                if (dp.db.isInTransaction) {
                    acc.publicKey = key
                    acc.keyHeight = -1
                    accountTable.insert(acc)
                }
                return true
            }
            Arrays.equals(acc.publicKey, key) -> return true
            acc.keyHeight == -1 -> {
                if (logger.isInfoEnabled) {
                    logger.info("DUPLICATE KEY!!!")
                    logger.info("Account key for {} was already set to a different one at the same height, current height is {}, rejecting new key", acc.id.toUnsignedString(), height)
                }
                return false
            }
            acc.keyHeight >= height -> {
                logger.info("DUPLICATE KEY!!!")
                if (dp.db.isInTransaction) {
                    if (logger.isInfoEnabled) {
                        logger.info("Changing key for account {} at height {}, was previously set to a different one at height {}", acc.id.toUnsignedString(), height, acc.keyHeight)
                    }
                    acc.publicKey = key
                    acc.keyHeight = height
                    accountTable.insert(acc)
                }
                return true
            }
            logger.isInfoEnabled -> {
                logger.info("DUPLICATE KEY!!!")
                logger.info("Invalid key for account {} at height {}, was already set to a different one at height {}", acc.id.toUnsignedString(), height, acc.keyHeight)
            }
        }
        return false
    }

    internal class SQLAccountAsset(rs: Record) : Account.AccountAsset(rs.get(ACCOUNT_ASSET.ACCOUNT_ID), rs.get(ACCOUNT_ASSET.ASSET_ID), rs.get(ACCOUNT_ASSET.QUANTITY), rs.get(ACCOUNT_ASSET.UNCONFIRMED_QUANTITY), accountAssetDbKeyFactory.newKey(rs.get(ACCOUNT_ASSET.ACCOUNT_ID), rs.get(ACCOUNT_ASSET.ASSET_ID)))

    internal inner class SqlAccount(record: Record) : Account(record.get(ACCOUNT.ID), accountDbKeyFactory.newKey(record.get(ACCOUNT.ID)), record.get(ACCOUNT.CREATION_HEIGHT)) {
        init {
            this.publicKey = record.get(ACCOUNT.PUBLIC_KEY)
            this.keyHeight = record.get(ACCOUNT.KEY_HEIGHT)
            this.balanceNQT = record.get(ACCOUNT.BALANCE)
            this.unconfirmedBalanceNQT = record.get(ACCOUNT.UNCONFIRMED_BALANCE)
            this.forgedBalanceNQT = record.get(ACCOUNT.FORGED_BALANCE)
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
