package brs.db.sql

import brs.entity.Asset
import brs.DependencyProvider
import brs.db.BurstKey
import brs.db.store.AssetStore
import brs.schema.Tables.ASSET
import org.jooq.DSLContext
import org.jooq.Record

class SqlAssetStore(private val dp: DependencyProvider) : AssetStore {
    override val assetDbKeyFactory: BurstKey.LongKeyFactory<Asset> = object : DbKey.LongKeyFactory<Asset>(ASSET.ID) {
        override fun newKey(asset: Asset): BurstKey {
            return asset.dbKey
        }
    }
    override val assetTable: EntitySqlTable<Asset>

    init {
        assetTable = object : EntitySqlTable<Asset>("asset", ASSET, assetDbKeyFactory, dp) {

            override fun load(ctx: DSLContext, record: Record): Asset {
                return SqlAsset(record)
            }

            override fun save(ctx: DSLContext, asset: Asset) {
                saveAsset(ctx, asset)
            }
        }
    }

    private fun saveAsset(ctx: DSLContext, asset: Asset) {
        ctx.insertInto(ASSET).set(ASSET.ID, asset.id).set(ASSET.ACCOUNT_ID, asset.accountId).set(ASSET.NAME, asset.name).set(ASSET.DESCRIPTION, asset.description).set(ASSET.QUANTITY, asset.quantity).set(ASSET.DECIMALS, asset.decimals).set(ASSET.HEIGHT, dp.blockchainService.height).execute()
    }

    override fun getAssetsIssuedBy(accountId: Long, from: Int, to: Int): Collection<Asset> {
        return assetTable.getManyBy(ASSET.ACCOUNT_ID.eq(accountId), from, to)
    }

    private inner class SqlAsset internal constructor(record: Record) : Asset(record.get(ASSET.ID), assetDbKeyFactory.newKey(record.get(ASSET.ID)), record.get(ASSET.ACCOUNT_ID), record.get(ASSET.NAME), record.get(ASSET.DESCRIPTION), record.get(ASSET.QUANTITY), record.get(ASSET.DECIMALS))
}
