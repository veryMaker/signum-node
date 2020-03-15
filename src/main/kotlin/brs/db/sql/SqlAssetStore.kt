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
            override fun load(record: Record) = Asset(
                record.get(ASSET.ID),
                assetDbKeyFactory.newKey(record.get(ASSET.ID)),
                record.get(ASSET.ACCOUNT_ID),
                record.get(ASSET.NAME),
                record.get(ASSET.DESCRIPTION),
                record.get(ASSET.QUANTITY),
                record.get(ASSET.DECIMALS))

            override fun save(ctx: DSLContext, entity: Asset) {
                ctx.insertInto(ASSET, ASSET.ID, ASSET.ACCOUNT_ID, ASSET.NAME, ASSET.DESCRIPTION, ASSET.QUANTITY, ASSET.DECIMALS, ASSET.HEIGHT)
                    .values(entity.id, entity.accountId, entity.name, entity.description, entity.quantity, entity.decimals, dp.blockchainService.height)
                    .execute()
            }

            override fun save(ctx: DSLContext, entities: Collection<Asset>) {
                val height = dp.blockchainService.height
                val query = ctx.insertInto(ASSET, ASSET.ID, ASSET.ACCOUNT_ID, ASSET.NAME, ASSET.DESCRIPTION, ASSET.QUANTITY, ASSET.DECIMALS, ASSET.HEIGHT)
                entities.forEach { entity ->
                    query.values(entity.id, entity.accountId, entity.name, entity.description, entity.quantity, entity.decimals, height)
                }
                query.execute()
            }
        }
    }

    override fun getAssetsIssuedBy(accountId: Long, from: Int, to: Int): Collection<Asset> {
        return assetTable.getManyBy(ASSET.ACCOUNT_ID.eq(accountId), from, to)
    }
}
