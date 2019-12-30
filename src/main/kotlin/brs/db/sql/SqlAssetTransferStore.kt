package brs.db.sql

import brs.db.AssetTransferStore
import brs.db.BurstKey
import brs.db.useDslContext
import brs.entity.AssetTransfer
import brs.entity.DependencyProvider
import brs.schema.Tables.ASSET_TRANSFER
import org.jooq.DSLContext
import org.jooq.Record

internal class SqlAssetTransferStore(private val dp: DependencyProvider) : AssetTransferStore {
    override val assetTransferTable: SqlEntityTable<AssetTransfer>
    override val transferDbKeyFactory: SqlDbKey.LongKeyFactory<AssetTransfer> = TransferDbKeyFactory

    init {
        assetTransferTable =
            object : SqlEntityTable<AssetTransfer>(ASSET_TRANSFER, transferDbKeyFactory, ASSET_TRANSFER.HEIGHT, null, dp) {
                override fun load(ctx: DSLContext, record: Record): AssetTransfer {
                    return SqlAssetTransfer(record)
                }

                override fun save(ctx: DSLContext, entity: AssetTransfer) {
                    saveAssetTransfer(entity)
                }
            }
    }

    private fun saveAssetTransfer(assetTransfer: AssetTransfer) {
        dp.db.useDslContext<Unit> { ctx ->
            ctx.insertInto(
                ASSET_TRANSFER,
                ASSET_TRANSFER.ID,
                ASSET_TRANSFER.ASSET_ID,
                ASSET_TRANSFER.SENDER_ID,
                ASSET_TRANSFER.RECIPIENT_ID,
                ASSET_TRANSFER.QUANTITY,
                ASSET_TRANSFER.TIMESTAMP,
                ASSET_TRANSFER.HEIGHT
            )
                .values(
                    assetTransfer.id,
                    assetTransfer.assetId,
                    assetTransfer.senderId,
                    assetTransfer.recipientId,
                    assetTransfer.quantity,
                    assetTransfer.timestamp,
                    assetTransfer.height
                )
                .execute()
        }
    }

    override fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer> {
        return assetTransferTable.getManyBy(ASSET_TRANSFER.ASSET_ID.eq(assetId), from, to)
    }

    override fun getAccountAssetTransfers(accountId: Long, from: Int, to: Int): Collection<AssetTransfer> {
        return dp.db.useDslContext { ctx ->
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
            SqlDbUtils.applyLimits(selectQuery, from, to)

            assetTransferTable.getManyBy(ctx, selectQuery, false)
        }
    }

    override fun getAccountAssetTransfers(
        accountId: Long,
        assetId: Long,
        from: Int,
        to: Int
    ): Collection<AssetTransfer> {
        return dp.db.useDslContext { ctx ->
            val selectQuery = ctx
                .selectFrom(ASSET_TRANSFER).where(
                    ASSET_TRANSFER.SENDER_ID.eq(accountId).and(ASSET_TRANSFER.ASSET_ID.eq(assetId))
                )
                .unionAll(
                    ctx.selectFrom(ASSET_TRANSFER).where(
                        ASSET_TRANSFER.RECIPIENT_ID.eq(accountId)
                    ).and(
                        ASSET_TRANSFER.SENDER_ID.ne(accountId)
                    ).and(ASSET_TRANSFER.ASSET_ID.eq(assetId))
                )
                .orderBy(ASSET_TRANSFER.HEIGHT.desc())
                .query
            SqlDbUtils.applyLimits(selectQuery, from, to)

            assetTransferTable.getManyBy(ctx, selectQuery, false)
        }
    }

    override fun getTransferCount(assetId: Long): Int {
        return dp.db.useDslContext { ctx ->
            ctx.fetchCount(
                ctx.selectFrom(ASSET_TRANSFER).where(
                    ASSET_TRANSFER.ASSET_ID.eq(
                        assetId
                    )
                )
            )
        }
    }

    internal inner class SqlAssetTransfer(record: Record) : AssetTransfer(
        record.get(ASSET_TRANSFER.ID),
        transferDbKeyFactory.newKey(record.get(ASSET_TRANSFER.ID)),
        record.get(ASSET_TRANSFER.ASSET_ID),
        record.get(ASSET_TRANSFER.HEIGHT),
        record.get(ASSET_TRANSFER.SENDER_ID),
        record.get(ASSET_TRANSFER.RECIPIENT_ID),
        record.get(ASSET_TRANSFER.QUANTITY),
        record.get(ASSET_TRANSFER.TIMESTAMP)
    )

    companion object {
        private object TransferDbKeyFactory : SqlDbKey.LongKeyFactory<AssetTransfer>(ASSET_TRANSFER.ID) {
            override fun newKey(entity: AssetTransfer): BurstKey {
                return entity.dbKey
            }
        }
    }
}
