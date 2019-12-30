package brs.db.sql

import brs.db.AssetStore
import brs.db.BurstKey
import brs.entity.Asset
import brs.entity.DependencyProvider
import brs.schema.Tables.ASSET
import org.jooq.DSLContext
import org.jooq.Record

internal class SqlAssetStore(private val dp: DependencyProvider) : AssetStore {
    override val assetDbKeyFactory: SqlDbKey.LongKeyFactory<Asset> = object : SqlDbKey.LongKeyFactory<Asset>(ASSET.ID) {
        override fun newKey(entity: Asset): BurstKey {
            return entity.dbKey
        }
    }
    override val assetTable: SqlEntityTable<Asset>

    init {
        assetTable = object : SqlEntityTable<Asset>(ASSET, assetDbKeyFactory, ASSET.HEIGHT, null, dp) {
            override fun load(ctx: DSLContext, record: Record): Asset {
                return SqlAsset(record)
            }

            override fun save(ctx: DSLContext, entity: Asset) {
                ctx.insertInto(ASSET).set(ASSET.ID, entity.id).set(ASSET.ACCOUNT_ID, entity.accountId).set(ASSET.NAME, entity.name)
                    .set(ASSET.DESCRIPTION, entity.description).set(ASSET.QUANTITY, entity.quantity)
                    .set(ASSET.DECIMALS, entity.decimals).set(ASSET.HEIGHT, dp.blockchainService.height).execute()
            }
        }
    }

    override fun getAssetsIssuedBy(accountId: Long, from: Int, to: Int): Collection<Asset> {
        return assetTable.getManyBy(ASSET.ACCOUNT_ID.eq(accountId), from, to)
    }

    private inner class SqlAsset internal constructor(record: Record) : Asset(
        record.get(ASSET.ID),
        assetDbKeyFactory.newKey(record.get(ASSET.ID)),
        record.get(ASSET.ACCOUNT_ID),
        record.get(ASSET.NAME),
        record.get(ASSET.DESCRIPTION),
        record.get(ASSET.QUANTITY),
        record.get(ASSET.DECIMALS)
    )
}
