package brs.db.sql

import brs.AssetTransfer
import brs.DependencyProvider
import brs.db.BurstKey
import brs.db.store.AssetTransferStore
import brs.schema.Tables.ASSET_TRANSFER
import org.jooq.DSLContext
import org.jooq.Record

class SqlAssetTransferStore(dp: DependencyProvider) : AssetTransferStore {
    override val assetTransferTable: EntitySqlTable<AssetTransfer>
    override val transferDbKeyFactory = TransferDbKeyFactory

    init {
        assetTransferTable = object : EntitySqlTable<AssetTransfer>("asset_transfer", brs.schema.Tables.ASSET_TRANSFER, transferDbKeyFactory, dp) {
            override fun load(ctx: DSLContext, record: Record): AssetTransfer {
                return SqlAssetTransfer(record)
            }

            override fun save(ctx: DSLContext, assetTransfer: AssetTransfer) {
                saveAssetTransfer(assetTransfer)
            }
        }
    }

    private fun saveAssetTransfer(assetTransfer: AssetTransfer) {
        Db.useDSLContext { ctx ->
            ctx.insertInto(ASSET_TRANSFER, ASSET_TRANSFER.ID, ASSET_TRANSFER.ASSET_ID, ASSET_TRANSFER.SENDER_ID, ASSET_TRANSFER.RECIPIENT_ID, ASSET_TRANSFER.QUANTITY, ASSET_TRANSFER.TIMESTAMP, ASSET_TRANSFER.HEIGHT)
                    .values(assetTransfer.id, assetTransfer.assetId, assetTransfer.senderId, assetTransfer.recipientId, assetTransfer.quantityQNT, assetTransfer.timestamp, assetTransfer.height)
                    .execute()
        }
    }

    override fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer> {
        return assetTransferTable.getManyBy(ASSET_TRANSFER.ASSET_ID.eq(assetId), from, to)
    }

    override fun getAccountAssetTransfers(accountId: Long, from: Int, to: Int): Collection<AssetTransfer> {
        return Db.useDSLContext<Collection<AssetTransfer>> { ctx ->
            val selectQuery = ctx
                    .selectFrom(ASSET_TRANSFER).where(
                            ASSET_TRANSFER.SENDER_ID.eq(accountId)
                    )
                    .unionAll(
                            ctx.selectFrom(ASSET_TRANSFER).where(
                                    ASSET_TRANSFER.RECIPIENT_ID.eq(accountId).and(ASSET_TRANSFER.SENDER_ID.ne(accountId))
                            )
                    )
                    .orderBy(ASSET_TRANSFER.HEIGHT.desc())
                    .query
            DbUtils.applyLimits(selectQuery, from, to)

            assetTransferTable.getManyBy(ctx, selectQuery, false)
        }
    }

    override fun getAccountAssetTransfers(accountId: Long, assetId: Long, from: Int, to: Int): Collection<AssetTransfer> {
        return Db.useDSLContext<Collection<AssetTransfer>> { ctx ->
            val selectQuery = ctx
                    .selectFrom(ASSET_TRANSFER).where(
                            ASSET_TRANSFER.SENDER_ID.eq(accountId).and(ASSET_TRANSFER.ASSET_ID.eq(assetId))
                    )
                    .unionAll(
                            ctx.selectFrom(ASSET_TRANSFER).where(
                                    ASSET_TRANSFER.RECIPIENT_ID.eq(accountId)).and(
                                    ASSET_TRANSFER.SENDER_ID.ne(accountId)
                            ).and(ASSET_TRANSFER.ASSET_ID.eq(assetId))
                    )
                    .orderBy(ASSET_TRANSFER.HEIGHT.desc())
                    .query
            DbUtils.applyLimits(selectQuery, from, to)

            assetTransferTable.getManyBy(ctx, selectQuery, false)
        }
    }

    override fun getTransferCount(assetId: Long): Int {
        return Db.useDSLContext<Int> { ctx -> ctx.fetchCount(ctx.selectFrom(ASSET_TRANSFER).where(ASSET_TRANSFER.ASSET_ID.eq(assetId))) }
    }

    internal inner class SqlAssetTransfer(record: Record) : AssetTransfer(record.get(ASSET_TRANSFER.ID), transferDbKeyFactory.newKey(record.get(ASSET_TRANSFER.ID)), record.get(ASSET_TRANSFER.ASSET_ID), record.get(ASSET_TRANSFER.HEIGHT), record.get(ASSET_TRANSFER.SENDER_ID), record.get(ASSET_TRANSFER.RECIPIENT_ID), record.get(ASSET_TRANSFER.QUANTITY), record.get(ASSET_TRANSFER.TIMESTAMP))

    companion object {
        object TransferDbKeyFactory : DbKey.LongKeyFactory<AssetTransfer>(ASSET_TRANSFER.ID) {
            override fun newKey(assetTransfer: AssetTransfer): BurstKey {
                return assetTransfer.dbKey
            }
        }
    }
}
